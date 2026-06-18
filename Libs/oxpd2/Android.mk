
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := oxpdlib
LOCAL_SDK_VERSION := 19
LOCAL_SRC_FILES := $(call all-java-files-under, java)

LOCAL_STATIC_JAVA_LIBRARIES := \
	nanohttpd \
	jabberwocky

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

include $(BUILD_STATIC_JAVA_LIBRARY)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := nanohttpd:nanohttpd-2.3.1.jar

include $(BUILD_MULTI_PREBUILT)
