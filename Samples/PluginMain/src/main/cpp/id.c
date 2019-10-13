#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <pwd.h>
#include <grp.h>
#include <android/log.h>
//#include <selinux/selinux.h>

#include "id.h"

#define LOG_TAG "whoami"

#define LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,LOG_TAG,__VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

static void print_uid(uid_t uid)
{
    struct passwd *pw = getpwuid(uid);

    if(pw) {
        LOGV("%d(%s)", uid, pw->pw_name);
    } else {
        LOGV("%d", uid);
    }
}

static void print_gid(gid_t gid)
{
    struct group *gr = getgrgid(gid);

    if(gr) {
        LOGV("%d(%s)", gid, gr->gr_name);
    } else {
        LOGV("%d", gid);
    }
}

int whoami()
{
    gid_t list[64];
    int n, max;

    max = getgroups(64, list);
    if (max < 0) max = 0;

    LOGV("uid=");
    print_uid(getuid());
    LOGV(" gid=");
    print_gid(getgid());
    if (max) {
        LOGV(" groups=");
        print_gid(list[0]);
        for(n = 1; n < max; n++) {
            print_gid(list[n]);
        }
    }
    LOGV("\n");
    return 0;
}