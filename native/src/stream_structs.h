#ifndef STREAM_STRUCTS_H
#define STREAM_STRUCTS_H

#include <pthread.h>
#include <stdint.h>
#include <stdlib.h>
#include <lsquic.h>
#include <stdio.h>
#include <string.h>

struct stream_ctx {
	uint64_t id; /* must be first! */
	/*
	 * for sending
	 */
	pthread_mutex_t* write_mutex;
	pthread_cond_t * write_cond;
	unsigned char* data;
	uint64_t amount;

	int cur_writes;

	/*
	 * for receiving
	 */
	pthread_mutex_t* read_mutex;
	pthread_cond_t * read_cond;
	unsigned char* read_data;
	uint64_t read_max_amount;
	uint64_t read_min_amount;

	int cur_reads;
};

/**
 * creates a new struct stream_ctx
 * @param id id of stream
 */
struct stream_ctx * create_stream_ctx(uint64_t id);

/**
 * Deletes a struct stream_ctx and all associated resources. <br>
 * Terminates all current read and write operations to the stream.
 */
void delete_stream_ctx(struct stream_ctx * context);

#endif

