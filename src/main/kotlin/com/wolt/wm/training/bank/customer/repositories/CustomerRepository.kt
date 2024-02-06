package com.wolt.wm.training.bank.customer.repositories

import com.wolt.wm.training.bank.customer.models.Address
import com.wolt.wm.training.bank.customer.models.Customer
import com.wolt.wm.training.bank.db.tables.records.CustomersRecord
import org.jooq.DSLContext
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.table
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
class CustomerRepository(private val ctx: DSLContext) {
    companion object {
        val customersTable = table("CUSTOMERS")
    }

    private fun mapRecordToCustomer(record: CustomersRecord): Customer {
        return Customer(
            id = record.getValue(field("id", UUID::class.java)),
            firstName = record.getValue(field("first_name", String::class.java)),
            lastName = record.getValue(field("last_name", String::class.java)),
            birthdate = record.getValue(field("birthdate", LocalDate::class.java)),
            gender = record.getValue(field("gender", String::class.java)),
            address =
                Address(
                    street = record.getValue(field("street_address", String::class.java)),
                    city = record.getValue(field("city", String::class.java)),
                    country = record.getValue(field("country", String::class.java)),
                    postalCode = record.getValue(field("postal_code", String::class.java)),
                ),
            email = record.getValue(field("email", String::class.java)),
            phone = record.getValue(field("phone", String::class.java)),
        )
    }

    fun getCustomers(
        name: String?,
        pageSize: Int,
        page: Int,
    ): List<Customer> {
        val offset = (page - 1) * pageSize
        val limit = pageSize
        val query = ctx.select().from(customersTable)

        if (!name.isNullOrBlank()) {
            query.where(
                field("first_name").like("%$name%")
                    .or(field("last_name").like("%$name%"))
                    .or(field("first_name").concat(" ").concat(field("last_name")).like("%$name%"))
                    .or(field("last_name").concat(" ").concat(field("first_name")).like("%$name%")),
            )
        }

        query.limit(limit).offset(offset)

        val result = query.fetchInto(CustomersRecord::class.java)

        return result.map {
            mapRecordToCustomer(it)
        }
    }

    fun getCustomer(customerId: UUID): Customer? {
        val query = ctx.select().from(customersTable).where(field("id").eq(customerId))
        val record = query.fetchOneInto(CustomersRecord::class.java) ?: return null

        return mapRecordToCustomer(record)
    }

    fun createCustomer(customer: Customer) {
        ctx.insertInto(
            customersTable,
            field("id"),
            field("first_name"),
            field("last_name"),
            field("birthdate"),
            field("gender"),
            field("street_address"),
            field("city"),
            field("country"),
            field("postal_code"),
            field("email"),
            field("phone"),
        )
            .values(
                customer.id,
                customer.firstName,
                customer.lastName,
                customer.birthdate,
                customer.gender,
                customer.address.street,
                customer.address.city,
                customer.address.country,
                customer.address.postalCode,
                customer.email,
                customer.phone,
            )
            .execute()
    }

    fun updateCustomer(customer: Customer) {
        ctx.update(customersTable)
            .set(field("first_name"), customer.firstName)
            .set(field("last_name"), customer.lastName)
            .set(field("birthdate"), customer.birthdate)
            .set(field("gender"), customer.gender)
            .set(field("street_address"), customer.address.street)
            .set(field("city"), customer.address.city)
            .set(field("country"), customer.address.country)
            .set(field("postal_code"), customer.address.postalCode)
            .set(field("email"), customer.email)
            .set(field("phone"), customer.phone)
            .where(field("id").eq(customer.id))
            .execute()
    }

    fun deleteCustomer(customerId: UUID) {
        ctx.deleteFrom(customersTable)
            .where(field("id").eq(customerId))
            .execute()
    }
}
