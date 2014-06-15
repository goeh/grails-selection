package test

class TestEntity2 {

    String name
    String postalCode
    String city
    Integer age

    static constraints = {
        name(maxSize: 100)
        postalCode(maxSize: 10, nullable: true)
        city(maxSize: 50, nullable: true)
        age(nullable: true)
    }

    String toString() {
        name
    }
}
