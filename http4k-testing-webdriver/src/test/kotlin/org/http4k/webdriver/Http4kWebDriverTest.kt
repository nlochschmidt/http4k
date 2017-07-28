package org.http4k.webdriver

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.present
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.junit.Test
import org.openqa.selenium.By
import java.io.File
import java.net.URL

class Http4kWebDriverTest {
    private val driver = Http4kWebDriver {
        req ->
        val body = File("src/test/resources/test.html").readText()
        Response(OK).body(body
            .replace("THEMETHOD", req.method.name)
            .replace("THEBODY", req.bodyString())
            .replace("THEURL", req.uri.path)
            .replace("THETIME", System.currentTimeMillis().toString())
        )
    }

    @Test
    fun `page details`() {
        driver.get("/bob")
        assertThat(driver.currentUrl, equalTo("/bob"))
        assertThat(driver.title, equalTo("Page title"))
        assertThat(driver.findElement(By.id("firstId"))!!.text, equalTo("the first text"))
    }

    @Test
    fun `POST form`() {
        driver.get("/bob")
        driver.findElement(By.id("button"))!!.submit()
        assertOnPage("/form")
        assertThat(driver.findElement(By.tagName("thebody"))!!.text, equalTo(""))
        assertThat(driver.findElement(By.tagName("themethod"))!!.text, equalTo("POST"))
    }

    @Test
    fun `navigation`() {
        driver.navigate().to("/rita")
        assertOnPage("/rita")

        driver.navigate().to(URL("http://localhost/bob"))
        assertOnPage("/bob")
        driver.get("/bill")
        assertOnPage("/bill")
        driver.navigate().back()
        assertOnPage("/bob")
        driver.navigate().forward()
        assertOnPage("/bill")
        val preRefreshTime = driver.findElement(By.tagName("h2"))!!.text
        driver.navigate().refresh()
        assertOnPage("/bill")
        assertThat(driver.findElement(By.tagName("h2"))!!.text, !equalTo(preRefreshTime))
    }

    @Test
    fun `single window lifecycle`() {
        assertThat(driver.windowHandles.size, equalTo(0))
        driver.get("/bill")
        assertThat(driver.windowHandles.size, equalTo(1))
        assertThat(driver.windowHandle, present())
        driver.quit()
        assertThat(driver.windowHandles.size, equalTo(0))
        driver.get("/bill")
        assertThat(driver.windowHandles.size, equalTo(1))
        assertThat(driver.windowHandle, present())
        driver.close()
        assertThat(driver.windowHandles.size, equalTo(0))
    }

    @Test
    fun `active element`() {
        driver.get("/bill")
        assertThat(driver.switchTo().activeElement().tagName, equalTo("div"))
    }

    @Test
    fun `click`() {
        driver.get("/bill")
        driver.findElement(By.tagName("a"))!!.click()
        assertOnPage("/link")
    }

    @Test
    fun `unsupported features`() {
        driver.get("/bill")

        val windowHandle = driver.windowHandle

        isNotImplemented {driver.manage()}
        isNotImplemented {driver.switchTo().alert()}
        isNotImplemented {driver.switchTo().frame(0)}
        isNotImplemented {driver.switchTo().frame("bob")}
        isNotImplemented {driver.switchTo().frame(windowHandle)}
        isNotImplemented {driver.switchTo().parentFrame()}
    }

    private fun assertOnPage(expected: String) {
        assertThat(driver.findElement(By.tagName("h1"))!!.text, equalTo(expected))
    }

}