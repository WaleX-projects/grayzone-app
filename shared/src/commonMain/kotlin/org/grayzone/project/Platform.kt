package org.grayzone.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform