cmake_minimum_required (VERSION 3.10)

project (asiojni)

SET( ASIO_SDK_BASE ../asiosdk ) 
include_directories($ENV{JAVA_HOME}/include)
include_directories($ENV{JAVA_HOME}/include/win32)
include_directories( ${ASIO_SDK_BASE}/host ${ASIO_SDK_BASE}/host/PC ${ASIO_SDK_BASE}/common )

add_subdirectory( ${ASIO_SDK_BASE} ${CMAKE_CURRENT_BINARY_DIR}/asiosdk )

add_library(${PROJECT_NAME} SHARED asiojni.cpp)
target_link_libraries(${PROJECT_NAME} asiosdk)