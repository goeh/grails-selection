grails.project.work.dir = "target"
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6

grails.project.dependency.resolution = {
    inherits("global") {}
    log "warn"
    legacyResolve false
    repositories {
        grailsCentral()
        mavenCentral()
    }
    dependencies {
        /*test("javax.validation:validation-api:1.1.0.Final") { export = false }
        test("org.hibernate:hibernate-validator:5.0.3.Final") { export = false }*/
        test('org.springframework:spring-expression:4.0.5.RELEASE') { export = false }
        test('org.springframework:spring-aop:4.0.5.RELEASE') { export = false }
        test('org.hamcrest:hamcrest-all:1.3') { export = false }
    }

    plugins {
        build(":release:3.1.2",
                ":rest-client-builder:2.1.1") {
            export = false
        }
        test(":hibernate4:4.3.6.1") {
            excludes "net.sf.ehcache:ehcache-core"  // remove this when http://jira.grails.org/browse/GPHIB-18 is resolved
            export = false
        }
    }
}
