#include <stdio.h>
#include <string.h>

static int
indexOf(const char *src, char c)
{
    if(NULL == src){
        return -1;
    }

    int i = 0;
    while(src[i]){
        if(c == src[i]){
            return i;
        }
        ++i;
    }

    return -1;
}

int
sunday(const char *src, const char *pat)
{
    if(NULL == src || NULL == pat){
        return -1;
    }
    int s_len = strlen(src);
    int p_len = strlen(pat);

    int idx;
    int s = 0, p = 0;

    while(s < s_len && p < p_len)
    {
        while(src[s] == pat[p]){
            ++s, ++p;
            if(p >= p_len){
                return s-p;
            }else if(s >= s_len){
                return -1;
            }
        }

        if((idx = indexOf(pat, src[s-p+p_len])) < 0){
            s = s-p+p_len+1;
        }else{
            s = s-p+p_len-idx;
        }
        p = 0;
    }
    return -1;
}
