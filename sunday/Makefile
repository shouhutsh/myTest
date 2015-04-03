
all: sunday sunday.o

sunday: test.c sunday.o
	gcc -o $@ $^ -g -Wall

sunday.o: sunday.c
	gcc -c -o $@ $^ -g -Wall

clean:
	@rm -rf sunday sunday.o
