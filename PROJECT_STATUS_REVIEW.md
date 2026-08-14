# PosterPro Backend — Plain-English Status Review

*Prepared for founders and business stakeholders. No coding knowledge required.*

---

## 1. What This Backend Actually Does

Think of this project as the **kitchen behind a restaurant**. Customers (the mobile app or website, which is built separately) never see this part — they only see the finished dish. This "kitchen" is the engine that will let jewellery and other retail shop owners create branded promotional posters (for festivals, gold rate updates, offers, etc.), store their shop details, and eventually pay for a subscription to unlock more designs.

Right now, this kitchen has its **plumbing, walk-in fridge, and the core stovetop (making posters) installed and working** — but some cooking stations (paying for a plan, getting notified) are **not yet built or connected**. What exists today: a secure way for shop owners to create an account and log in, saving their shop's current gold rates, browsing/searching/favoriting the design catalogue, and — as of this update — actually **generating a finished poster image** from a template, the shop's gold rates, and their logo. Getting notifications and paying for subscriptions still only have a "storage shelf" reserved for them in the database, with no working door (API) that lets the app actually use them yet.

---

## 2. What's Built and Working Right Now

For each feature below: a plain description, and whether there is evidence in the code that it can actually be used today.

### ✅ Shop Owner Account Creation ("Register")
A shop owner can create an account by giving an email, a password (minimum 8 characters), and their shop name. The system checks the email isn't already registered and safely scrambles ("hashes") the password so it's never stored in readable form — standard, responsible practice, comparable to how a bank never keeps a copy of your PIN in plain text.
**Status:** Built and reachable — the code path is complete and would work if called from the app.

### ✅ Shop Owner Login
A shop owner logs in with email and password. If correct, the system hands back a digital "wristband" (technically called a JWT — a signed, tamper-proof pass) that the app then shows on every future request instead of asking for the password again. The wristband expires automatically after 24 hours, at which point they'd need to log in again.
**Status:** Built and reachable.

### ✅ Gold Rate Settings
Each shop owner can set and update their current gold rates for four purities (22k, 18k, 14k, and 9k gold) — this is presumably meant to be pulled into posters automatically so a jeweller doesn't have to manually update the rate on every graphic. A shop owner can fetch their saved rates or overwrite them.
**Status:** Built and reachable.

### ✅ Browse Jewellery Categories (NEW)
A shop owner can now fetch the list of jewellery-type categories (e.g., Rings, Earrings, Necklaces, Bangles) to power a category filter, and look up a single category by ID.
**Status:** Built, reachable, and manually tested against a running local server and database — confirmed working (details in Section 2a below).

### ✅ Browse & Search Poster Templates (NEW)
A shop owner can now browse the catalogue of poster templates, filter by jewellery-type category, search by name, and page through results. Each template card returns its image, name, price, and whether the current shop owner has already favorited it. A single template can also be fetched in full detail.
**Status:** Built, reachable, and manually tested — confirmed working.

### ✅ Favorites (NEW)
A shop owner can now mark a template as a favorite, remove it, and view their full list of favorited templates.
**Status:** Built, reachable, and manually tested — confirmed working.

### ✅ Poster Generation & Downloads (NEW)
A shop owner can now generate an actual poster image: pick a template, optionally override their saved gold rates for that specific poster (matching the "Set Rates" screen), and the backend composites the template's background art with the shop's name, gold rates, and logo (if one is set) into a finished PNG or JPG. The finished image is stored privately and handed back as a time-limited (1 hour) link the app can load directly. Every successful generation is also logged to the Downloads history.
**Status:** Built, reachable, and manually tested against a running local server, database, and file storage — confirmed working end-to-end, including the shop-logo overlay. **Important caveat:** this only works for templates that actually have real design data attached (a background image + layout positions) — see the note in Section 5. Right now that's true for exactly **one** of the four sample templates in the catalogue; the rest correctly return a clear "not ready" error instead of a broken or blank image. PDF export is intentionally not implemented yet (would require adding a PDF library, which was out of scope for this round) — requesting it returns a clear error rather than a broken file.

### ⚠️ Everything Else Below: Data Storage Exists, But No Working Feature Yet
For every item in this section, a "storage shelf" (database table) has been built and is ready to hold real data, and in some cases partial logic exists — but there is **no door (API endpoint) that the app can currently call** to use the feature. In practical terms: a shop owner cannot receive a notification, subscribe to a plan, or pay for anything today, because the connecting piece hasn't been written yet.

- **Notifications** (in-app alerts, e.g., "New festival template available") — shelf exists, no way to send, list, or mark them as read.
- **Subscriptions** (Free/Basic/Premium/Enterprise plan tiers) — shelf exists, no way to create, check, or manage a subscription.
- **Payments** — a partially-written connector to a payment processor called Razorpay exists behind the scenes (explained in Section 5), but it is not wired up to anything the app can call. A shop owner cannot pay for anything right now.
- **File Storage** — the file storage connector is now actively used by poster generation (reading template backgrounds and shop logos, writing finished posters, and generating time-limited download links). What's still missing is a **shop owner logo upload endpoint** — there's a reserved spot for a logo on the shop profile, and poster generation will use it if it's set, but there's still no door for a shop owner to actually upload one. Today the only way a logo gets attached is manually inserting a storage key into the database, which is how this was verified.

### 2a. What We Verified Building Categories, Templates & Favorites

A follow-up build added working "doors" (API endpoints) for Categories, Templates, and Favorites — three of the items previously listed as "storage only." Before writing any code, we checked what the frontend app that's supposed to consume this backend actually expects. **That frontend project (referred to as "poster-pro-demo") could not be located anywhere on this machine** — no folder, no repository, nothing to inspect. Rather than guess at its exact expectations, the endpoints below were built to match the plain-English feature description we were given, with every assumption called out explicitly. **These should be treated as a first draft that needs a side-by-side check against the real frontend once it's available**, not a guaranteed exact match.

**On the "price" field:** The poster template storage shelf (the `templates` database table) had no field for a price before this work — it only stored the design itself (title, image, layout data, which category it belongs to, festival tagging, which subscription plan unlocks it). Since a price that changes per template can't be something the frontend simply hardcodes on its own, we added one new column to the database (`price`) to hold it. **This is the one database change made in this round of work**, and it was made because the feature genuinely couldn't work without it — not guessed at silently.

**On the "BIS 916 hallmark" badge:** No equivalent field exists on the templates table, and unlike price, a certification badge like this is very plausibly the *same fixed label* shown on every card (all templates in this catalogue are presumably for BIS-916-hallmarked gold jewellery, so the badge wouldn't need to vary per template the way a price does). Because we couldn't inspect the frontend to confirm one way or the other, **we did not add a database field for this** — adding one on a guess would risk exactly the kind of invented column this task asked us to avoid. If the badge does need to vary per template in the future (e.g., some templates being silver or a different certification), that would be a small follow-up column to add.

**On category data:** The category storage shelf was completely empty — zero rows, no seed data of any kind was ever built into the project. So there was nothing to check for a mismatch against "jewellery type"; there was simply nothing there. Four sample categories (Rings, Earrings, Necklaces, Bangles) and one sample template under each were added as a proper, permanent part of the project's database setup scripts (not just typed into the local database by hand) — so a brand-new copy of this project, or a freshly deployed database, will automatically come with this same sample data already in place, and `GET /api/categories` / `GET /api/templates` will work immediately without anyone manually adding rows. This was confirmed by wiping the local database entirely and re-running the setup from scratch. **This is still placeholder/sample data, not real catalogue content** — see Section 5 for what needs deciding before real users see this.

### A Note on "Tested"
The two original features (register/login, gold rate settings) still have **no automated tests that verify they work** — only a technical check that the application is able to start up. That doesn't mean they're broken; based on reading the code, they look correctly and carefully written, but there's no test evidence proving they've been exercised end-to-end.

The three new features (Categories, Templates, Favorites) are different: they were **manually exercised against a real, running local server and a real local database** during this work — registering a test shop account, logging in, listing and filtering categories and templates, searching, viewing template detail, adding/removing/listing favorites, and checking that invalid input (like a negative page number) is correctly rejected. One real bug was caught and fixed this way (a search filter that crashed the server when no search term was given — it worked fine once fixed). This is stronger evidence than the original two features have, though it's still one developer's manual pass, not an automated safety net that protects against future changes breaking it.

---

## 3. What's in the Database

A database is like a set of labeled filing cabinets. Here is what each cabinet is for, in plain terms — not the technical column names, just what real-world information lives there:

| Cabinet (Table) | What It Actually Stores |
|---|---|
| **users** | Each shop owner's account: login email, scrambled password, shop name, shop phone number, shop address, a spot for a logo image, and the type of business. |
| **categories** | The list of poster categories a shop owner could browse by (e.g., festival posters vs. offer posters), with a name, a URL-friendly short name, an icon, and a display order. |
| **templates** | Each individual poster design: which category it belongs to, its title, a small public preview thumbnail, a separate private full-resolution background image used to actually generate the poster, the layout data describing where the shop name/rates/logo get placed, whether it's tied to a specific festival (and which date), whether it's currently active/visible, and which subscription plan a shop owner needs to unlock it. |
| **favorites** | A simple link recording "this shop owner liked this template" — used to power a "My Favorites" screen. |
| **gold_rate_profiles** | Each shop's current gold rate figures for the four purities, plus when they were last updated. |
| **subscriptions** | A record of which plan (Free, Basic, Premium, Enterprise) a shop owner is on, and the start and end date of that plan period. |
| **payments** | A record of each payment attempt: which shop owner, which subscription it was for, which payment provider handled it, the amount, the currency, and whether it succeeded, failed, or is still pending. |
| **downloads** | A log of every time a shop owner exports a finished poster: which template, in what file format (PNG, JPG, or PDF), and when. |
| **notifications** | Messages sent to a shop owner (a title and body text), and whether they've read it yet. |

All nine cabinets exist and are properly connected to each other (e.g., a favorite always points to a real user and a real template, and the database itself enforces this — it's not just trusted to the app to get right). This is a genuinely solid, well-thought-out foundation to build on. The gap is entirely on the "features that use these cabinets" side, not the cabinets themselves.

---

## 4. What's Missing / Not Built Yet

Comparing against the original product plan (categories, templates, favorites, notifications, subscriptions, payments, downloads):

| Planned Feature | Database Table? | Working API? | Verdict |
|---|:---:|:---:|---|
| User accounts (register/login) | ✅ | ✅ | **Done** |
| Gold rate settings | ✅ | ✅ | **Done** |
| Categories | ✅ | ✅ | **Done** — browse and view a single category. Table now ships with 4 sample categories via a database migration (see Section 5 — this is placeholder data, not real catalogue content) |
| Templates (poster designs) | ✅ (+ new `price` column) | ✅ | **Done** — browse, filter by category, search by name, paginate, view detail. Required adding a `price` column (was missing) — see Section 2a |
| Favorites | ✅ | ✅ | **Done** — add, remove (both idempotent), and list a shop owner's favorited templates |
| Poster generation & Downloads | ✅ (+ new `background_image_key` column) | ✅ | **Done** — generates a real PNG/JPG poster (background + shop name + gold rates + logo) and logs it to Downloads. Required adding a `background_image_key` column (was missing) — see Section 5 for the catalogue-data caveat |
| Notifications | ✅ | ❌ | Storage only — nothing to send or read notifications with |
| Subscriptions (plan tiers) | ✅ | ❌ | Storage only — nothing to subscribe, upgrade, or check plan status with |
| Payments | ✅ | ❌ (started, unreachable) | A payment-provider connector exists in the code but nothing in the app can trigger it yet — see Section 5 |
| File uploads (shop logo, poster images) | ✅ (a spot for the logo URL exists on the user record) | ⚠️ (partial) | Poster images are generated and stored automatically; there's still no endpoint for a shop owner to upload their own logo — see Section 5 |

**Bottom line, stated plainly:** account creation, gold rate settings, browsing/searching templates/categories/favorites, and now poster generation all work. What's still missing is subscribing to a paid plan, paying for it, getting notified, and letting a shop owner upload their own logo — none of that exists yet as something a user could touch, even though the database is fully ready to receive it. The previous single biggest gap — **no poster-generation capability at all** — is now closed for templates that have real design data; the remaining catalogue-data gap is tracked in Section 5.

---

## 5. Risks and Things to Watch

- **Payment system is not real yet, and its test keys are placeholders.** The code that would eventually talk to the Razorpay payment processor is written, but the account credentials it uses (`key-id` / `key-secret`) currently fall back to literal placeholder text (`rzp_test_placeholder`, `placeholder_secret_32chars_min`) if real ones aren't supplied. Nothing catastrophic today since nothing calls this code yet, but this is a clear flag: **do not treat payments as functional or safe until real Razorpay credentials are wired in through a secure, non-checked-in configuration, and the payment flow is actually connected to an endpoint, tested, and verified against real transactions.**

- **Security keys are currently hardcoded for local development.** The secret key used to sign the login "wristband" (JWT), the local database password, and the local file-storage password are all written directly into a configuration file rather than kept in a secure, separate vault. This is completely normal and expected for a developer's own laptop during early building — but it must **not** be reused as-is once this system is deployed anywhere real users can reach it. A production deployment needs its own separately-managed, secret credentials that are never stored in the codebase.

- **No cross-origin permission setup found.** When the separate frontend (website/app) is hosted on a different address than this backend — which is the normal setup — web browsers require the backend to explicitly say "yes, this frontend is allowed to talk to me." No such permission is currently configured. In practice this likely means that when someone tries to connect the actual frontend website to this backend, it may simply refuse to work until this is added. This is a quick fix, but it will very likely surface as a "nothing loads" bug the first time frontend and backend are connected.

- **No password reset, no email verification.** A shop owner can register with any email address (even one they don't own) and there's no way to verify it or recover a forgotten password. This is normal for an early-stage build but should be treated as a requirement before opening this up to real, paying shop owners — otherwise, a locked-out customer has no self-service way back in.

- **Every logged-in user currently has identical permissions.** There is no concept yet of an admin vs. a regular shop owner — anyone with a valid login "wristband" can, in principle, be granted access to anything that gets built later. This isn't a problem with what exists today (there's nothing sensitive to restrict yet), but it needs to be designed deliberately before, for example, an admin-only "manage templates" feature is added.

- **No automated tests exist beyond "does the app start."** As noted in Section 2, there is currently zero automated proof that any feature — including the two that are built — behaves correctly. Bugs could be introduced by future changes with nothing to catch them.

- **The one existing test requires a live database to even run.** It's not currently possible to verify this project "works" on a fresh machine without first starting up the supporting database and file-storage systems. This isn't unusual this early on, but it means quality-checking currently depends entirely on a human manually testing against a running setup — there's no quick, automatic check.

- **No shop-logo upload endpoint exists yet**, despite the user profile having a reserved spot for one, and despite poster generation now being able to use a logo once it's set. Any "upload your logo" button in the app would currently have nothing to connect to — for now, the only way a logo gets attached to a shop is by inserting a storage key directly into the database, which is how this was tested.

- **Only 1 of the 4 sample templates currently has real design data — the other 3 cannot generate a poster yet.** Poster generation needs two things per template that didn't previously exist: a private, print-resolution background image (`background_image_key`) and a layout describing where to draw the shop name, gold rates, and logo (`schema_json`, documented in `docs/TEMPLATE_SCHEMA_JSON.md`). Populating a full catalogue of real templates with these is a separate, ongoing content task, not something that can be inferred from code — it needs actual designed artwork per template. In the meantime, the endpoint correctly refuses to generate for a template that isn't ready (a clear error, not a blank or broken image), so this fails safely rather than shipping a bad poster — but **before real users see this, the remaining templates (and any real catalogue added later) need this design data filled in, or "Generate Poster" will simply not work for them.**

- **The category and template catalogue currently contains sample/placeholder data only, and it will now travel with the project everywhere.** To make the new browsing/search/favorites APIs testable and usable out of the box, 4 sample jewellery categories (Rings, Earrings, Necklaces, Bangles) and one sample template per category (with made-up names, image links, and prices) were added as a permanent part of the database setup — meaning every future copy of this database (a teammate's laptop, a staging server, production) will automatically include this same placeholder data unless someone changes it. **Before real users see this app, someone needs to explicitly decide**: keep this data as-is (fine for a demo), replace it with the real jewellery catalogue, or remove it and load real content through a proper admin process instead. Shipping a "Diamond Drop Earrings" placeholder template with a made-up price to real paying shop owners would look unfinished if left in by accident.

- **The exact shape these new endpoints should return could not be confirmed against the real frontend.** We could not locate the "poster-pro-demo" frontend project anywhere on this machine to check its exact expected field names and response format. The new Categories, Templates, and Favorites endpoints were built from the written feature description instead, with assumptions clearly flagged (see Section 2a). **Before wiring the real frontend up to these endpoints, do a side-by-side check** that field names line up exactly (e.g., confirm whether the frontend expects `thumbnailUrl` or `imageUrl`, `isFavorited` or `favorited`, etc.) — a mismatch here is a quick fix, but it will otherwise show up as "the page loads blank" the first time they're connected.

---

## 6. In Plain Numbers

Counting each major planned feature as one unit and scoring it **Fully Working**, **Partially Built** (database exists but the feature can't actually be used), or **Not Started**:

| # | Feature | Status |
|---|---|---|
| 1 | User registration & login | ✅ Fully Working |
| 2 | Gold rate settings | ✅ Fully Working |
| 3 | Categories | ✅ Fully Working *(newly built and manually verified; underlying data still needs real content)* |
| 4 | Templates (browse/view designs) | ✅ Fully Working *(newly built and manually verified; required adding a `price` field)* |
| 5 | Favorites | ✅ Fully Working *(newly built and manually verified)* |
| 6 | Poster generation & downloads | ✅ Fully Working *(newly built and manually verified end-to-end, including the logo overlay; but only usable today for 1 of 4 sample templates until the rest get real design data — see Section 5)* |
| 7 | Notifications | ⚠️ Partially Built |
| 8 | Subscriptions (plan tiers) | ⚠️ Partially Built |
| 9 | Payments | ⚠️ Partially Built |
| 10 | File storage / logo upload | ⚠️ Partially Built *(storage itself now actively used by poster generation; shop-owner logo upload endpoint still missing)* |

**Score: 6 out of 10 features (60%) are fully working. 4 out of 10 (40%) have their database groundwork done but no usable feature yet. 0 out of 10 have zero work done at all — every planned feature has at least its storage designed.**

In short: the foundation (database design, security/login system, and project structure) is solid, and browsing/searching/favoriting the product catalogue now works end-to-end. What's left is turning a browsed template into an actual finished poster, downloading it, subscribing to a paid plan, paying for it, and getting notified — and the single most important missing piece is still the ability to generate a poster at all, which is presumably the whole point of the app.
