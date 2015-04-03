#include <stdio.h>

int
main(int argc, char *argv[])
{
    if(3 != argc){
        printf("Usage: ./sunday src pat");
        return 0;
    }
    char * src = argv[1];
    char * pat = argv[2];

    int idx = sunday(src, pat);

    if(-1 != idx){
        printf("src: %s\npat: %s\n", src, pat);
        printf("%d: %s\n", idx, src+idx);
    }else{
        printf("error!\n");
    }

    return 0;
}
