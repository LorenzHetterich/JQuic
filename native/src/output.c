/**
 * write to quic streams
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


/**
 * stream write callback
 */
void stream_write_cb(lsquic_stream_t* stream, struct stream_ctx* context){

	mutex_lock(context->write_mutex);

	int amount = lsquic_stream_write(stream, context->data, context->amount);

	if(amount <= 0){
		// TODO: error handling!
	}

	context->amount -= amount;

	if(!context->amount){
		lsquic_stream_wantwrite(stream, 0);
		signal_cond(context->write_cond);
		mutex_unlock(context->write_mutex);
		return;
	}

	lsquic_stream_wantwrite(stream, 1);
	context->data += amount;

	mutex_unlock(context->write_mutex);
}

void stream_write(lsquic_stream_t* stream, struct stream_ctx* context, unsigned char* data, uint32_t offset, uint32_t length){
	mutex_lock(context->write_mutex);
	if(context->cur_writes){
		printf("MULTIPLE WRITES!!!!!\n");
		fflush(stdout);
	}
	context->cur_writes ++;
	context->amount = length;
	context->data = data + offset;
}

uint32_t stream_write_wait(struct stream_ctx* context){
	if(context->amount){
		wait_cond(context->write_cond, context->write_mutex);
	}

	context->cur_writes --;
	mutex_unlock(context->write_mutex);
	return 0;
}


