-- Records which plan tier a checkout was FOR, independent of the Subscription
-- it eventually creates. Needed because a Payment is created at checkout time
-- (before we know the payment will succeed) and the Subscription row is only
-- created once /verify (or the webhook) confirms the Razorpay signature — we
-- need to remember what the user was actually paying for in between.
ALTER TABLE payments ADD COLUMN plan_tier VARCHAR(20) NOT NULL DEFAULT 'BASIC';
ALTER TABLE payments ALTER COLUMN plan_tier DROP DEFAULT;
