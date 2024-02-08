package com.wolt.wm.training.bank.customer.repositories

import com.wolt.wm.training.bank.customer.models.Address
import com.wolt.wm.training.bank.customer.models.Customer
import com.wolt.wm.training.bank.db.tables.records.CustomersRecord
import com.wolt.wm.training.bank.db.tables.references.CUSTOMERS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class CustomerRepository(private val ctx: DSLContext) {
    private fun CustomersRecord.intoCustomer(): Customer {
        return Customer(
            id = id!!,
            firstName = firstName!!,
            lastName = lastName!!,
            birthdate = birthdate!!,
            gender = gender,
            address =
                Address(
                    street = streetAddress!!,
                    city = city!!,
                    country = country!!,
                    postalCode = postalCode,
                ),
            email = email,
            phone = phone,
        )
    }

    private fun Customer.intoRecord(): CustomersRecord {
        return CustomersRecord().also {
            it.id = id
            it.firstName = firstName
            it.lastName = lastName
            it.birthdate = birthdate
            it.gender = gender
            it.streetAddress = address.street
            it.city = address.city
            it.country = address.country
            it.postalCode = address.postalCode
            it.email = email
            it.phone = phone
        }
    }

    fun getCustomers(
        name: String?,
        pageSize: Int,
        page: Int,
    ): List<Customer> {
        val offset = (page - 1) * pageSize
        val limit = pageSize
        val query = ctx.selectFrom(CUSTOMERS)

        if (!name.isNullOrBlank()) {
            query.where(
                CUSTOMERS.FIRST_NAME.contains(name)
                    .or(CUSTOMERS.LAST_NAME.contains(name))
                    .or(CUSTOMERS.FIRST_NAME.concat(" ").concat(CUSTOMERS.LAST_NAME).contains(name)),
            )
        }

        query.limit(limit).offset(offset)

        val result = query.fetch()

        return result.map { it.intoCustomer() }
    }

    fun findCustomer(customerId: UUID): Customer? {
        val record =
            ctx.selectFrom(CUSTOMERS).where(CUSTOMERS.ID.eq(customerId)).fetchOne()
                ?: return null

        return record.intoCustomer()
    }

    fun createCustomer(customer: Customer): Customer {
        val result =
            ctx.insertInto(CUSTOMERS)
                .set(customer.intoRecord())
                .returning()
                .fetchOne()

        return result!!.intoCustomer()
    }

    fun updateCustomer(customer: Customer) {
        ctx.update(CUSTOMERS)
            .set(customer.intoRecord())
            .where(CUSTOMERS.ID.eq(customer.id))
            .execute()
    }

    fun deleteCustomer(customerId: UUID) {
        ctx.deleteFrom(CUSTOMERS)
            .where(CUSTOMERS.ID.eq(customerId))
            .execute()
    }
}
