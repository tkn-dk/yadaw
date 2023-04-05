set(ASIOSDK_SOURCES
    common/asio.cpp
    host/asiodrivers.cpp
    host/pc/asiolist.cpp
)

include_directories( host host/PC common )
add_library(asiosdk STATIC ${ASIOSDK_SOURCES})