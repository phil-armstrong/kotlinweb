package uk.co.boombastech.kotlinweb.http.servlets

import uk.co.boombastech.kotlinweb.http.DataResponse
import uk.co.boombastech.kotlinweb.http.GlobalFiltersFactory
import uk.co.boombastech.kotlinweb.http.Response
import uk.co.boombastech.kotlinweb.http.filters.PostControllerFilter
import uk.co.boombastech.kotlinweb.http.filters.PostResponseFilter
import uk.co.boombastech.kotlinweb.http.filters.PreControllerFilter
import uk.co.boombastech.kotlinweb.http.requests.RequestFactory
import uk.co.boombastech.kotlinweb.http.routing.RouteFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Singleton
class ControllerServlet @Inject constructor(private val routeFactory: RouteFactory, private val requestFactory: RequestFactory, private val globalFilters: GlobalFiltersFactory) : HttpServlet() {

    override fun service(servletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {
        val request = requestFactory.get(servletRequest)
        val filters = globalFilters.getFilters()
        var response: Response

        try {
            val requestHandler = routeFactory.find(request)

            filters.filterIsInstance(PreControllerFilter::class.java).forEach { filter -> filter.preControllerFilter(request) }
            requestHandler.filters.filterIsInstance(PreControllerFilter::class.java).forEach { filter -> filter.preControllerFilter(request) }

            response = requestHandler.controller.execute(request)

            filters.filterIsInstance(PostControllerFilter::class.java).forEach { filter -> filter.postControllerFilter(request, response) }
            requestHandler.filters.filterIsInstance(PostControllerFilter::class.java).forEach { filter -> filter.postControllerFilter(request, response) }

            request.cookies.updated().forEach({ cookie ->
                servletResponse.addCookie(cookie.toServletCookie())
            })
        } catch (exception: Exception) {
            if (exception is WebException) {
                response = exception.execute(request)
            } else {
                println("Exception thrown, was type ${exception.javaClass.kotlin}")
                response = DataResponse("none rendering exception")
            }
        }

        servletResponse.status = response.httpStatus.code

        response.headers.forEach { entry ->
            servletResponse.addHeader(entry.key, entry.value)
        }

        when (response) {
            is DataResponse -> servletResponse.writer.print(response.data)
        }

        filters.filterIsInstance(PostResponseFilter::class.java).forEach { filter -> filter.postResponseFilter(request, response) }
    }
}