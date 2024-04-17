package io.alex.bank.customer.repositories

import io.alex.bank.customer.models.Address
import io.alex.bank.customer.models.Customer
import io.alex.bank.db.tables.records.CustomerRecord
import io.alex.bank.db.tables.references.CUSTOMER
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class CustomerRepository(private val ctx: DSLContext) {
    fun CustomerRecord.toDomain(): Customer {
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
            active = active!!,
        )
    }

    fun Customer.toRecord(): CustomerRecord {
        return CustomerRecord().also {
            it.id = id
            it.firstName = firstName
            it.lastName = lastName
            it.fullName = "$firstName $lastName"
            it.birthdate = birthdate
            it.gender = gender
            it.streetAddress = address.street
            it.city = address.city
            it.country = address.country
            it.postalCode = address.postalCode
            it.email = email
            it.phone = phone
            it.active = active
        }
    }

    private fun upsertCustomer(customer: Customer): Customer {
        val result =
            ctx.insertInto(CUSTOMER)
                .set(customer.toRecord())
                .onConflict(CUSTOMER.ID)
                .doUpdate()
                .set(customer.toRecord())
                .returning()
                .fetchOne()

        return result!!.toDomain()
    }

    fun getCustomers(
        name: String?,
        pageSize: Int,
        page: Int,
    ): List<Customer> {
        val offset = (page - 1) * pageSize
        val limit = pageSize
        val query = ctx.selectFrom(CUSTOMER)

        query.where(CUSTOMER.ACTIVE.eq(true))

        if (!name.isNullOrBlank()) {
            query.where(CUSTOMER.FULL_NAME.containsIgnoreCase(name))
        }

        query.limit(limit).offset(offset)

        val result = query.fetch()

        return result.map { it.toDomain() }
    }

    fun findCustomer(customerId: UUID): Customer? {
        val record =
            ctx.selectFrom(CUSTOMER).where(CUSTOMER.ID.eq(customerId)).and(CUSTOMER.ACTIVE.eq(true)).fetchOne()
                ?: return null

        return record.toDomain()
    }

    fun createCustomer(customer: Customer): Customer = upsertCustomer(customer)

    fun updateCustomer(customer: Customer): Customer = upsertCustomer(customer)

    fun deleteCustomer(customerId: UUID) {
        ctx.update(CUSTOMER)
            .set(CUSTOMER.ACTIVE, false)
            .where(CUSTOMER.ID.eq(customerId))
            .execute()
    }
}
