create table ACCOUNT (
     id UUID PRIMARY KEY,
     customer_id UUID NOT NULL,
     balance DECIMAL(19, 2) NOT NULL CHECK (type = 'CREDIT' OR balance >= 0),
     currency TEXT NOT NULL,
     type TEXT NOT NULL,
     status TEXT NOT NULL,
     created_at DATE NOT NULL,
     updated_at DATE,
     FOREIGN KEY (customer_id) REFERENCES CUSTOMER(id)
);
