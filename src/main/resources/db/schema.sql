PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS staff (
                                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                                     username TEXT NOT NULL UNIQUE,
                                     password_hash TEXT NOT NULL,
                                     role TEXT NOT NULL CHECK (role IN ('MANAGER','BARTENDER','WAITRESS')),
    active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0,1)),
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
    );




-- optional: ensure only 2 digits (SQLite doesn't enforce CHECK well on ALTER, so we'll enforce in Java too)
-- optional: you can also add a unique index if you want each PIN unique:
CREATE UNIQUE INDEX IF NOT EXISTS ux_staff_pin_code ON staff(pin_code) WHERE pin_code IS NOT NULL;



CREATE TABLE IF NOT EXISTS login_audit (
                                           id INTEGER PRIMARY KEY AUTOINCREMENT,
                                           staff_id INTEGER,
                                           username_attempted TEXT NOT NULL,
                                           success INTEGER NOT NULL CHECK (success IN (0,1)),
    ts TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (staff_id) REFERENCES staff(id)
    );


CREATE TABLE IF NOT EXISTS products (
                                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                                        name TEXT NOT NULL,
                                        barcode TEXT UNIQUE,
                                        price_ex_cents INTEGER NOT NULL,
                                        vat_rate INTEGER NOT NULL,
                                        stock_qty INTEGER NOT NULL DEFAULT 0,
                                        active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0,1))


    );



CREATE TABLE IF NOT EXISTS sales (
                                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                                     created_at TEXT NOT NULL DEFAULT (datetime('now')),
    staff_id INTEGER NOT NULL,
    total_ex_cents INTEGER NOT NULL,
    total_vat_cents INTEGER NOT NULL,
    total_inc_cents INTEGER NOT NULL,
    cash_received_cents INTEGER NOT NULL,
    change_given_cents INTEGER NOT NULL,
    FOREIGN KEY (staff_id) REFERENCES staff(id)
    );

CREATE TABLE IF NOT EXISTS sale_items (
                                          id INTEGER PRIMARY KEY AUTOINCREMENT,
                                          sale_id INTEGER NOT NULL,
                                          product_id INTEGER NOT NULL,
                                          name_snapshot TEXT NOT NULL,
                                          qty INTEGER NOT NULL,
                                          unit_price_ex_cents INTEGER NOT NULL,
                                          vat_rate INTEGER NOT NULL,
                                          line_ex_cents INTEGER NOT NULL,
                                          line_vat_cents INTEGER NOT NULL,
                                          line_inc_cents INTEGER NOT NULL,
                                          FOREIGN KEY (sale_id) REFERENCES sales(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
    );


CREATE TABLE IF NOT EXISTS stock_movements (
                                 id INTEGER PRIMARY KEY AUTOINCREMENT,
                                 product_id INTEGER NOT NULL,
                                 staff_id INTEGER,
                                 qty_change INTEGER NOT NULL,        -- +10 restock, -2 waste, -1 sale
                                 reason TEXT NOT NULL,               -- RESTOCK / WASTE / SALE
                                 created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 FOREIGN KEY (product_id) REFERENCES products(id),
                                 FOREIGN KEY (staff_id) REFERENCES staff(id)
);

CREATE TABLE IF NOT EXISTS product_categories (
                                                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                  name TEXT NOT NULL UNIQUE,
                                                  color_hex TEXT NOT NULL DEFAULT '#4B7BEC'
);


-- (SQLite may not allow ALTER in all cases depending on existing file; if it errors, tell me and I'll give the safe migration steps.)

CREATE TABLE IF NOT EXISTS quick_buttons (
                                             id INTEGER PRIMARY KEY AUTOINCREMENT,
                                             page INTEGER NOT NULL DEFAULT 1,
                                             position INTEGER NOT NULL,                -- 0..N (row-major)
                                             type TEXT NOT NULL CHECK(type IN ('PRODUCT','CATEGORY')),
    product_id INTEGER,
    category_id INTEGER,
    label TEXT,                               -- optional override
    color_hex TEXT,                           -- optional override
    UNIQUE(page, position),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (category_id) REFERENCES product_categories(id)
    );

CREATE TABLE IF NOT EXISTS stock_items (
                                           id INTEGER PRIMARY KEY AUTOINCREMENT,
                                           name TEXT NOT NULL UNIQUE,
                                           category_name TEXT,
                                           base_unit TEXT NOT NULL,              -- ml / unit
                                           stock_unit_name TEXT NOT NULL,        -- bottle / keg / can / case
                                           stock_unit_size INTEGER NOT NULL,     -- 700 for 700ml bottle, 50000 for 50L keg
                                           current_qty_base_units INTEGER NOT NULL DEFAULT 0,
                                           cost_price_cents_per_stock_unit INTEGER NOT NULL DEFAULT 0,
                                           active INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS product_recipe_items (
                                                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                    product_id INTEGER NOT NULL,
                                                    stock_item_id INTEGER NOT NULL,
                                                    qty_base_units_used INTEGER NOT NULL,
                                                    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (stock_item_id) REFERENCES stock_items(id)
    );

CREATE TABLE IF NOT EXISTS stock_deliveries (
                                                id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                stock_item_id INTEGER NOT NULL,
                                                qty_stock_units INTEGER NOT NULL,
                                                unit_cost_cents INTEGER NOT NULL,
                                                staff_id INTEGER,
                                                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                FOREIGN KEY (stock_item_id) REFERENCES stock_items(id),
    FOREIGN KEY (staff_id) REFERENCES staff(id)
    );

CREATE TABLE IF NOT EXISTS stock_item_movements (
                                                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                    stock_item_id INTEGER NOT NULL,
                                                    staff_id INTEGER,
                                                    qty_change_base_units INTEGER NOT NULL,
                                                    reason TEXT NOT NULL,
                                                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                    FOREIGN KEY (stock_item_id) REFERENCES stock_items(id),
    FOREIGN KEY (staff_id) REFERENCES staff(id)
    );