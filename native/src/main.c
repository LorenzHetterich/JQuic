
#include "stream_structs.h"
#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include <lsquic.h>

struct lsquic_logger_if logger;

/**
 *
 */

int do_log(void* ctx, const char* buf, size_t len){
	fwrite(buf, len, 1, stdout);
	fflush(stdout);
	return 0;
}

void init_logging(){
	logger.log_buf = do_log;
	lsquic_logger_init(&logger, NULL, LLTS_HHMMSSMS);
}
