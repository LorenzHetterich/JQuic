#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

int run_jar(char ** args, int argc){

	char* argv[argc + 3];
	argv[0] = "java";
	argv[1] = "-jar";
	argv[2] = "jquic.jar";
	argv[argc + 2] = NULL;

	for(int i = 1; i < argc; i++){
		argv[2+i] = args[i];
	}

	execvp("java", argv);

	return 0;
}

int run_tests(){

	char* argv[5];
	argv[0] = "java";
	argv[1] = "-cp";
	argv[2] = "jquic.jar";
	argv[3] = "test.Main";
	argv[4] = NULL;

	execvp("java", argv);

	return 0;
}

int run_term() {
	system("cd IPTables;sh term.sh");
	return 0;
}

int main(int argc, char ** argv){

	if(argc >= 2){
		if(!strcmp(argv[1], "terminal")){
			return run_term();
		}
		if(!strcmp(argv[1], "tests")){
			return run_tests();
		}
	}

	return run_jar(argv, argc);
}
