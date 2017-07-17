
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog
LOCAL_MODULE    := SecureUtil
LOCAL_SRC_FILES := com_haolb_client_utils_SecureUtil.c

include $(BUILD_SHARED_LIBRARY)
