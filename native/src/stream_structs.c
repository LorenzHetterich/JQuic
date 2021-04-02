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
 * creates a new struct stream_ctx
 * @param id id of stream
 */
struct stream_ctx * create_stream_ctx(uint64_t id){

	/*
	 * generic
	 */
	struct stream_ctx* context = malloc(sizeof(struct stream_ctx));
	context->id = id;
	/*
	 * sending
	 */
#if defined(__linux__)
	context->write_mutex = malloc(sizeof(pthread_mutex_t));
	context->write_cond = malloc(sizeof(pthread_cond_t));
	pthread_mutex_init(context->write_mutex, NULL);
	pthread_cond_init(context->write_cond, NULL);
#endif

#if defined(_WIN32)
    context->write_mutex = malloc(sizeof(CRITICAL_SECTION));
    context->write_cond = malloc(sizeof(CONDITION_VARIABLE));
    InitializeCriticalSection(context->write_mutex);
    InitializeConditionVariable(context->write_cond);
#endif

	context->data = NULL;
	context->amount = 0;
	context->cur_writes = 0;

	/*
	 * receiving
	 */
#if defined(__linux__)
	context->read_mutex = malloc(sizeof(pthread_mutex_t));
	context->read_cond = malloc(sizeof(pthread_cond_t));
	pthread_mutex_init(context->read_mutex, NULL);
	pthread_cond_init(context->read_cond, NULL);
#endif
#if defined(_WIN32)
	 context->read_mutex = malloc(sizeof(CRITICAL_SECTION));
	 context->read_cond = malloc(sizeof(CONDITION_VARIABLE));
	 InitializeCriticalSection(context->read_mutex);
	 InitializeConditionVariable(context->read_cond);
#endif
	context->read_data = NULL;
	context->read_min_amount = 0;
	context->read_max_amount = 0;
	context->cur_reads = 0;

	return context;
}

/**
 * Deletes a struct stream_ctx and all associated resources. <br>
 * Terminates all current read and write operations to the stream.
 */
void delete_stream_ctx(struct stream_ctx * context){

	/*
	 * sending
	 */
	mutex_lock(context->write_mutex);
	context->amount = 0;
	// terminate current write operation
	while(context->cur_writes){
		mutex_unlock(context->write_mutex);
		signal_cond(context->write_cond);
		mutex_lock(context->write_mutex);
	}
    mutex_unlock(context->write_mutex);
    // destroy mutex and condition
#if defined(__linux__)
    pthread_cond_destroy(context->write_cond);
    pthread_mutex_destroy(context->write_mutex);
#endif
#if defined(_WIN32)
    DeleteCriticalSection(context->write_mutex);
#endif
    // free memory of mutex and condition
    free(context->write_cond);
    free(context->write_mutex);

    /*
     * receiving
     */
    mutex_lock(context->read_mutex);
	context->read_min_amount = 0;
	// terminate current read operation
	while(context->cur_reads){
		mutex_unlock(context->read_mutex);
		signal_cond(context->read_cond);
		mutex_lock(context->read_mutex);
	}
    mutex_unlock(context->read_mutex);
    // destroy mutex and condition
#if defined(__linux__)
    pthread_cond_destroy(context->read_cond);
    pthread_mutex_destroy(context->read_mutex);
#endif
#if defined(_WIN32)
    DeleteCriticalSection(context->read_mutex);
#endif
    // free memory of mutex and condition
    free(context->read_cond);
    free(context->read_mutex);

    /*
     * generic
     */
    // free memory of context struct itself
    // free(context);
}
