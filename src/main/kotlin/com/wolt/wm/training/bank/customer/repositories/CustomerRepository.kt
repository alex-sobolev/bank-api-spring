package com.wolt.wm.training.bank.customer.repositories

import com.wolt.wm.training.bank.customer.models.Address
import com.wolt.wm.training.bank.customer.models.Customer
import com.wolt.wm.training.bank.db.tables.records.CustomerRecord
import com.wolt.wm.training.bank.db.tables.references.CUSTOMER
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class CustomerRepository(private val ctx: DSLContext) {
    private fun CustomerRecord.intoCustomer(): Customer {
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

    private fun Customer.intoRecord(): CustomerRecord {
        return CustomerRecord().also {
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
        val query = ctx.selectFrom(CUSTOMER)

        if (!name.isNullOrBlank()) {
            query.where(
                CUSTOMER.FIRST_NAME.contains(name)
                    .or(CUSTOMER.LAST_NAME.contains(name))
                    .or(CUSTOMER.FIRST_NAME.concat(" ").concat(CUSTOMER.LAST_NAME).contains(name)),
            )
        }

        query.limit(limit).offset(offset)

        val result = query.fetch()

        return result.map { it.intoCustomer() }
    }

    fun findCustomer(customerId: UUID): Customer? {
        val record =
            ctx.selectFrom(CUSTOMER).where(CUSTOMER.ID.eq(customerId)).fetchOne()
                ?: return null

        return record.intoCustomer()
    }

    fun createCustomer(customer: Customer): Customer {
        val result =
            ctx.insertInto(CUSTOMER)
                .set(customer.intoRecord())
                .returning()
                .fetchOne()

        return result!!.intoCustomer()
    }

    fun updateCustomer(customer: Customer): Customer {
        val result =
            ctx.update(CUSTOMER)
                .set(customer.intoRecord())
                .where(CUSTOMER.ID.eq(customer.id))
                .returning()
                .fetchOne()

        return result!!.intoCustomer()
    }

    fun deleteCustomer(customerId: UUID) {
        ctx.deleteFrom(CUSTOMER)
            .where(CUSTOMER.ID.eq(customerId))
            .execute()
    }
}
