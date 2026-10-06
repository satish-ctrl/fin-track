CREATE TABLE users(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY ,
    email VARCHAR(255) UNIQUE NOT NULL ,
    username VARCHAR(50) UNIQUE NOT NULL ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE accounts(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY ,
    user_id BIGINT NOT NULL REFERENCES users(id) ,
    name VARCHAR(50) NOT NULL ,
    type VARCHAR(20) NOT NULL CHECK ( type IN ('CASH' , 'BANK')) ,
    balance NUMERIC(15,2) NOT NULL DEFAULT 0.00 CHECK ( balance >= 0 ) ,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK ( status IN ('ACTIVE' , 'INACTIVE')) ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE categories(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY ,
    user_id BIGINT NOT NULL REFERENCES users(id),
    name VARCHAR(50) NOT NULL ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, name)
);

CREATE TABLE transactions(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY ,
    user_id BIGINT NOT NULL REFERENCES users(id),
    type VARCHAR(20) NOT NULL CHECK ( type IN ('INCOME', 'EXPENSE', 'TRANSFER')),
    amount NUMERIC(15,2) NOT NULL CHECK ( amount > 0 ),
    from_account BIGINT REFERENCES accounts(id),
    to_account BIGINT REFERENCES accounts(id),
    category_id BIGINT REFERENCES categories(id),
    transaction_date DATE NOT NULL ,
    description VARCHAR(255) ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CHECK ( from_account IS NULL
            OR to_account IS NULL
            OR from_account <> to_account
    )
);