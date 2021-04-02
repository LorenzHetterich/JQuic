/**
 * read from quic streams
 */

#include "stream_structs.h"
#if defined(__linux__)
#include <pthread.h>
#endif
#include <stdint.h>
#include <stdlib.h>
#include <lsquic.h>
#include <stdio.h>
#include <string.h>


void stream_read_cb(lsquic_stream_t* stream, struct stream_ctx* context){

	mutex_lock(context->read_mutex);

	int amount = lsquic_stream_read(stream, context->read_data, context->read_max_amount);

	if(amount <= 0){
		// TODO: error handling!
	}

	context->read_max_amount -= amount;

	if(amount >= context->read_min_amount){
		context->read_min_amount = 0;
		lsquic_stream_wantread(stream, 0);
		signal_cond(context->read_cond);
		mutex_unlock(context->read_mutex);
		return;
	}

	context->read_min_amount -= amount;
	lsquic_stream_wantread(stream, 1);
	context->read_data += amount;

	mutex_unlock(context->read_mutex);
}

void stream_read(lsquic_stream_t* stream, struct stream_ctx* context, unsigned char* data, uint32_t min, uint32_t max){
	mutex_lock(context->read_mutex);
	if(context->cur_reads){
		printf("MULTIPLE READS!!!!!\n");
		fflush(stdout);
	}
	context->cur_reads ++;
	context->read_min_amount = min;
	context->read_max_amount = max;
	context->read_data = data;
}

uint32_t stream_read_wait(struct stream_ctx* context, uint32_t amount){

	if(context->read_min_amount){
		wait_cond(context->read_cond, context->read_mutex);
	}

	uint32_t ret = amount - (uint32_t)context->read_max_amount;

	context->cur_reads --;
	mutex_unlock(context->read_mutex);

	return ret;
}
