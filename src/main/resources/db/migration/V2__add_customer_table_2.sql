create table CUSTOMERS (
    id UUID PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    birthdate DATE,
    gender VARCHAR(20),
    email VARCHAR(255),
    phone VARCHAR(50),
    street_address VARCHAR(255),
    city VARCHAR(50),
    country VARCHAR(50),
    postal_code VARCHAR(50)
);
