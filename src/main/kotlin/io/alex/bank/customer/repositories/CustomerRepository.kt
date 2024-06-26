package io.alex.bank.customer.repositories

import io.alex.bank.customer.models.Address
import io.alex.bank.customer.models.Customer
import io.alex.bank.customer.models.CustomerStatus
import io.alex.bank.db.tables.records.CustomerRecord
import io.alex.bank.db.tables.references.CUSTOMER
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class CustomerRepository(
    private val ctx: DSLContext,
) {
    fun CustomerRecord.toDomain(): Customer =
        Customer(
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
            status = CustomerStatus.valueOf(status!!),
        )

    fun Customer.toRecord(): CustomerRecord =
        CustomerRecord().also {
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
            it.status = status.name
        }

    private fun upsertCustomer(customer: Customer): Customer {
        val result =
            ctx
                .insertInto(CUSTOMER)
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

        query.where(CUSTOMER.STATUS.eq(CustomerStatus.ACTIVE.name))

        if (!name.isNullOrBlank()) {
            query.where(CUSTOMER.FULL_NAME.containsIgnoreCase(name))
        }

        query.limit(limit).offset(offset)

        val result = query.fetch()

        return result.map { it.toDomain() }
    }

    fun findCustomer(
        customerId: UUID,
        includeInactive: Boolean = false,
    ): Customer? {
        val query =
            ctx
                .selectFrom(CUSTOMER)
                .where(CUSTOMER.ID.eq(customerId))

        if (!includeInactive) {
            query.and(CUSTOMER.STATUS.eq(CustomerStatus.ACTIVE.name))
        }

        val record = query.fetchOne() ?: return null

        return record.toDomain()
    }

    fun createCustomer(customer: Customer): Customer = upsertCustomer(customer)

    fun updateCustomer(customer: Customer): Customer = upsertCustomer(customer)

    fun deleteCustomer(customerId: UUID): Int =
        ctx
            .update(CUSTOMER)
            .set(CUSTOMER.STATUS, CustomerStatus.INACTIVE.name)
            .where(CUSTOMER.ID.eq(customerId))
            .execute()
}
