#ifndef STREAM_STRUCTS_H
#define STREAM_STRUCTS_H

#if defined(__linux__)
#include <pthread.h>
#endif


#if defined(_WIN32)
#include <synchapi.h>
#include <SDKDDKVer.h>
#include <winsock2.h>
#include <Windows.h>
#endif

#include <stdint.h>
#include <stdlib.h>
#include <lsquic.h>
#include <stdio.h>
#include <string.h>

#if defined(__linux__)
#define mutex_lock(x) pthread_mutex_lock(x)
#define mutex_unlock(x) pthread_mutex_unlock(x)
#define wait_cond(x, l) pthread_cond_wait(x, l)
#define signal_cond(x) pthread_cond_signal(x)
#endif

#if defined(_WIN32)
void WakeConditionVariable(PCONDITION_VARIABLE);
BOOL SleepConditionVariableCS(PCONDITION_VARIABLE, PCRITICAL_SECTION, DWORD);
void InitializeConditionVariable(PCONDITION_VARIABLE ConditionVariable);

#define mutex_lock(x) EnterCriticalSection(x)
#define mutex_unlock(x) LeaveCriticalSection(x)
#define wait_cond(x, l) SleepConditionVariableCS(x, l, INFINITE)
#define signal_cond(x) WakeConditionVariable(x)
#endif

struct stream_ctx {
	uint64_t id; /* must be first! */
	/*
	 * for sending
	 */

	#if defined(__linux__)
	pthread_mutex_t* write_mutex;
	pthread_cond_t * write_cond;
	#endif

	#if defined(_WIN32)
	CRITICAL_SECTION* write_mutex;
	CONDITION_VARIABLE*  write_cond;
	#endif

	unsigned char* data;
	uint64_t amount;

	int cur_writes;

	/*
	 * for receiving
	 */
	#if defined(__linux__)
	pthread_mutex_t* read_mutex;
	pthread_cond_t * read_cond;
	#endif

	#if defined(_WIN32)
	CRITICAL_SECTION* read_mutex;
	CONDITION_VARIABLE*  read_cond;
	#endif
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

