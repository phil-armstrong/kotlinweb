package uk.co.boombastech.kotlinweb.http

import com.google.inject.servlet.GuiceFilter
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.servlet.ServletContextHandler
import uk.co.boombastech.kotlinweb.http.config.Config
import uk.co.boombastech.kotlinweb.http.routing.Routes
import java.util.*
import javax.servlet.DispatcherType

fun main(args: Array<String>) {
    val config = Config()
    val server = Server(config.getProperty(Config.server.port))

    val servletContextHandler = ServletContextHandler(ServletContextHandler.SESSIONS);
    servletContextHandler.contextPath = "/"
    servletContextHandler.addEventListener(Listener(config))
    servletContextHandler.addFilter(GuiceFilter::class.java, "/*", EnumSet.allOf(DispatcherType::class.java))

    servletContextHandler.addServlet(DefaultServlet::class.java, "/")

    server.handler = servletContextHandler

    server.start()
    server.join()
}

class WebModuleTest : WebModule() {
    override fun getRoutes(): Routes {
        return Routes()
    }

    override fun wiring() {

    }
}