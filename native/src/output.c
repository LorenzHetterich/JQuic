/**
 * write to quic streams
 */

#include <pthread.h>
#include <stdint.h>
#include <stdlib.h>
#include <lsquic.h>
#include <stdio.h>
#include <string.h>

#include "stream_structs.h"

/**
 * stream write callback
 */
void stream_write_cb(lsquic_stream_t* stream, struct stream_ctx* context){

	pthread_mutex_lock(context->write_mutex);

	int amount = lsquic_stream_write(stream, context->data, context->amount);

	if(amount <= 0){
		// TODO: error handling!
	}

	context->amount -= amount;

	if(!context->amount){
		lsquic_stream_wantwrite(stream, 0);
		pthread_cond_signal(context->write_cond);
		pthread_mutex_unlock(context->write_mutex);
		return;
	}

	lsquic_stream_wantwrite(stream, 1);
	context->data += amount;

	pthread_mutex_unlock(context->write_mutex);
}

void stream_write(lsquic_stream_t* stream, struct stream_ctx* context, unsigned char* data, uint32_t offset, uint32_t length){
	pthread_mutex_lock(context->write_mutex);
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
		pthread_cond_wait(context->write_cond, context->write_mutex);
	}

	context->cur_writes --;
	pthread_mutex_unlock(context->write_mutex);
	return 0;
}


