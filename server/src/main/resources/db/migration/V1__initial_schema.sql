CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE households (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    invite_code VARCHAR(32) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE household_members (
    household_id UUID NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('OWNER', 'MEMBER')),
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (household_id, user_id)
);
CREATE INDEX household_members_user_idx ON household_members(user_id);

CREATE TABLE products (
    id UUID PRIMARY KEY,
    household_id UUID NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(80),
    unit VARCHAR(20) NOT NULL CHECK (unit IN ('PIECE', 'GRAM', 'MILLILITER')),
    minimum_quantity NUMERIC(12, 3) NOT NULL DEFAULT 0 CHECK (minimum_quantity >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (id, household_id)
);
CREATE INDEX products_household_idx ON products(household_id);

CREATE TABLE product_batches (
    id UUID PRIMARY KEY,
    household_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity NUMERIC(12, 3) NOT NULL CHECK (quantity >= 0),
    storage VARCHAR(20) NOT NULL CHECK (storage IN ('FRIDGE', 'FREEZER', 'CABINET', 'PANTRY')),
    purchased_on DATE,
    expires_on DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    FOREIGN KEY (product_id, household_id) REFERENCES products(id, household_id) ON DELETE CASCADE
);
CREATE INDEX product_batches_product_idx ON product_batches(product_id);
CREATE INDEX product_batches_expiry_idx ON product_batches(household_id, expires_on);

CREATE TABLE shopping_items (
    id UUID PRIMARY KEY,
    household_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity NUMERIC(12, 3) NOT NULL CHECK (quantity > 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('OPEN', 'PURCHASED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    purchased_at TIMESTAMPTZ,
    FOREIGN KEY (product_id, household_id) REFERENCES products(id, household_id) ON DELETE CASCADE
);
CREATE INDEX shopping_items_household_idx ON shopping_items(household_id, status);
