#!/usr/bin/env bash
# Uploads a thumbnail + background JPG pair to MinIO and generates a Flyway
# migration that inserts the corresponding `templates` row.
#
# Requires: Docker Desktop running (uses the `minio/mc` image — no local
# install needed) and this backend's MinIO already up on the configured
# endpoint (see storage.s3.* in application.yml).
#
# Usage:
#   scripts/templates/add-template.sh \
#     --slug rose-gold-band \
#     --title "Rose Gold Wedding Band" \
#     --category rings \
#     --price 45000 \
#     --plan-tier FREE \
#     --thumbnail /path/to/thumb.jpg \
#     --background /path/to/full-res.jpg \
#     [--canvas-width 1080] [--canvas-height 1350] \
#     [--endpoint http://host.docker.internal:9000] [--test-base-url http://localhost:9000]
#
# `--endpoint` is what the mc container uses to reach MinIO (defaults to
# host.docker.internal, which resolves to your host machine from inside
# Docker Desktop). The DB only ever gets a bucket-relative object key
# (templates.thumbnail_url) — the backend resolves that to a full URL per
# environment via storage.s3.public-base-url in application.yml, so this
# script never needs to know which host your client will use.
# `--test-base-url` is only used to print a curl command at the end so you
# can sanity-check the upload; it defaults to localhost since that's what's
# reachable from wherever you're running this script.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MIGRATIONS_DIR="$SCRIPT_DIR/../../src/main/resources/db/migration"

BUCKET="posterpro"
ENDPOINT="http://host.docker.internal:9000"
TEST_BASE_URL="http://localhost:9000"
ACCESS_KEY="minioadmin"
SECRET_KEY="minioadmin"
CANVAS_WIDTH=1080
CANVAS_HEIGHT=1350
PLAN_TIER="FREE"
PRICE=""
SLUG=""
TITLE=""
CATEGORY=""
THUMBNAIL=""
BACKGROUND=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --slug) SLUG="$2"; shift 2 ;;
    --title) TITLE="$2"; shift 2 ;;
    --category) CATEGORY="$2"; shift 2 ;;
    --price) PRICE="$2"; shift 2 ;;
    --plan-tier) PLAN_TIER="$2"; shift 2 ;;
    --thumbnail) THUMBNAIL="$2"; shift 2 ;;
    --background) BACKGROUND="$2"; shift 2 ;;
    --canvas-width) CANVAS_WIDTH="$2"; shift 2 ;;
    --canvas-height) CANVAS_HEIGHT="$2"; shift 2 ;;
    --endpoint) ENDPOINT="$2"; shift 2 ;;
    --test-base-url) TEST_BASE_URL="$2"; shift 2 ;;
    *) echo "Unknown argument: $1" >&2; exit 1 ;;
  esac
done

for required in SLUG TITLE CATEGORY PRICE THUMBNAIL BACKGROUND; do
  if [[ -z "${!required}" ]]; then
    echo "Missing required --${required,,}" >&2
    exit 1
  fi
done

[[ -f "$THUMBNAIL" ]] || { echo "Thumbnail not found: $THUMBNAIL" >&2; exit 1; }
[[ -f "$BACKGROUND" ]] || { echo "Background not found: $BACKGROUND" >&2; exit 1; }

BG_EXT="${BACKGROUND##*.}"
THUMB_KEY="templates/thumbnails/${SLUG}.jpg"
BG_KEY="templates/backgrounds/${SLUG}.${BG_EXT}"

MC() {
  docker run --rm \
    --add-host=host.docker.internal:host-gateway \
    -v "$(dirname "$THUMBNAIL")":/thumb \
    -v "$(dirname "$BACKGROUND")":/bg \
    -e MC_HOST_local="http://${ACCESS_KEY}:${SECRET_KEY}@${ENDPOINT#http://}" \
    minio/mc "$@"
}

echo "==> Ensuring bucket '$BUCKET' exists"
MC mb --ignore-existing "local/${BUCKET}"

echo "==> Making templates/thumbnails/* publicly readable (idempotent)"
MC anonymous set download "local/${BUCKET}/templates/thumbnails" || true

echo "==> Uploading thumbnail -> ${THUMB_KEY} (public)"
MC cp "/thumb/$(basename "$THUMBNAIL")" "local/${BUCKET}/${THUMB_KEY}"

echo "==> Uploading background -> ${BG_KEY} (private — used only via presigned URL for poster generation)"
MC cp "/bg/$(basename "$BACKGROUND")" "local/${BUCKET}/${BG_KEY}"

# Next Flyway version = highest existing V<N>__ + 1
LAST_VERSION=$(ls "$MIGRATIONS_DIR" | grep -oE '^V[0-9]+' | grep -oE '[0-9]+' | sort -n | tail -1)
NEXT_VERSION=$((LAST_VERSION + 1))
SLUG_SNAKE=$(echo "$SLUG" | tr '-' '_')
OUT_FILE="$MIGRATIONS_DIR/V${NEXT_VERSION}__add_template_${SLUG_SNAKE}.sql"

sed \
  -e "s#{{TITLE}}#${TITLE}#g" \
  -e "s#{{CATEGORY_SLUG}}#${CATEGORY}#g" \
  -e "s#{{THUMBNAIL_KEY}}#${THUMB_KEY}#g" \
  -e "s#{{BACKGROUND_KEY}}#${BG_KEY}#g" \
  -e "s#{{PRICE}}#${PRICE}#g" \
  -e "s#{{PLAN_TIER}}#${PLAN_TIER}#g" \
  -e "s#{{CANVAS_WIDTH}}#${CANVAS_WIDTH}#g" \
  -e "s#{{CANVAS_HEIGHT}}#${CANVAS_HEIGHT}#g" \
  -e "s#{{CANVAS_WIDTH_MINUS_180}}#$((CANVAS_WIDTH - 180))#g" \
  -e "s#{{CANVAS_HEIGHT_MINUS_170}}#$((CANVAS_HEIGHT - 170))#g" \
  -e "s#{{CANVAS_HEIGHT_MINUS_115}}#$((CANVAS_HEIGHT - 115))#g" \
  -e "s#{{CANVAS_HEIGHT_MINUS_80}}#$((CANVAS_HEIGHT - 80))#g" \
  -e "s#{{CANVAS_HEIGHT_MINUS_200}}#$((CANVAS_HEIGHT - 200))#g" \
  "$SCRIPT_DIR/migration.sql.template" > "$OUT_FILE"

echo ""
echo "==> Wrote $OUT_FILE"
echo "    thumbnail_url (key):  $THUMB_KEY"
echo "    background_image_key: $BG_KEY"
echo ""
echo "Sanity-check the public thumbnail upload:"
echo "    curl -I ${TEST_BASE_URL}/${BUCKET}/${THUMB_KEY}   # expect HTTP 200"
echo ""
echo "Review the placeholder x/y coordinates in the generated file against"
echo "the actual artwork (they're generic defaults), then run:"
echo "    mvn -q flyway:migrate   # or just restart the app, flyway runs on boot"
