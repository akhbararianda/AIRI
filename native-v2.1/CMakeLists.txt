cmake_minimum_required(VERSION 3.24)
project(AIRIDownloadManagerNative VERSION 2.8.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)
if(MSVC)
    add_compile_options(/utf-8 /W4 /permissive-)
endif()

add_library(airi_common STATIC
    src/common/airi_json.cpp
    src/common/airi_text.cpp
    src/common/airi_ranges.cpp
)
target_include_directories(airi_common PUBLIC src)

enable_testing()
add_executable(airi_core_tests tests/core_tests.cpp)
target_link_libraries(airi_core_tests PRIVATE airi_common)
add_test(NAME airi_core_tests COMMAND airi_core_tests)

if(WIN32)
    add_library(airi_engine STATIC
        src/engine/download_engine_v272_fixed.cpp
        src/engine/media_worker.cpp
    )
    target_include_directories(airi_engine PUBLIC src)
    target_compile_definitions(airi_engine PRIVATE UNICODE _UNICODE WIN32_LEAN_AND_MEAN NOMINMAX)
    target_link_libraries(airi_engine PUBLIC airi_common winhttp shlwapi shell32)

    add_library(airi_license STATIC
        src/app/licensing_v3.cpp
        src/app/licensing_cloud_access.cpp
    )
    target_include_directories(airi_license PUBLIC src)
    target_compile_definitions(airi_license PRIVATE UNICODE _UNICODE WIN32_LEAN_AND_MEAN NOMINMAX)
    target_link_libraries(airi_license PUBLIC airi_common bcrypt advapi32)

    add_library(airi_cloud STATIC src/app/license_cloud_v2.cpp)
    target_include_directories(airi_cloud PUBLIC src)
    target_compile_definitions(airi_cloud PRIVATE UNICODE _UNICODE WIN32_LEAN_AND_MEAN NOMINMAX)
    target_link_libraries(airi_cloud PUBLIC airi_license airi_common winhttp bcrypt advapi32)

    add_executable(airi_license_tests tests/license_tests.cpp)
    target_compile_definitions(airi_license_tests PRIVATE UNICODE _UNICODE WIN32_LEAN_AND_MEAN NOMINMAX)
    target_link_libraries(airi_license_tests PRIVATE airi_license)
    add_test(NAME airi_license_tests COMMAND airi_license_tests)

    add_executable(AIRIDownloadManager WIN32
        src/app/main_v26_airi.cpp
        src/app/license_cloud_agent.cpp
        src/app/app.rc
    )
    target_compile_definitions(AIRIDownloadManager PRIVATE UNICODE _UNICODE WIN32_LEAN_AND_MEAN NOMINMAX)
    target_link_libraries(AIRIDownloadManager PRIVATE airi_engine airi_license airi_cloud airi_common comctl32 shell32 shlwapi dwmapi ole32 bcrypt winhttp)
    set_target_properties(AIRIDownloadManager PROPERTIES OUTPUT_NAME "AIRI Download Manager")

    add_executable(AIRIDMBridge WIN32 src/bridge/main.cpp)
    target_compile_definitions(AIRIDMBridge PRIVATE UNICODE _UNICODE WIN32_LEAN_AND_MEAN NOMINMAX)
    target_link_libraries(AIRIDMBridge PRIVATE airi_common shell32 shlwapi)
endif()
