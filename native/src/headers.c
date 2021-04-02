#if defined(_WIN32)
#include <winsock2.h>
#endif

#include <lsxpack_header.h>
#include <lsquic.h>
#include <stdlib.h>
#include <string.h>

#include <stdio.h>


int send_headers(lsquic_stream_t* stream, char** headers, int amount){
	struct lsquic_http_headers real;
	real.count = amount;
	struct lsxpack_header* hdrs = malloc(amount * sizeof(struct lsxpack_header));
	real.headers = hdrs;
	int size = 0;
	for(int i = 0; i < amount; i++){
		size += strlen(headers[2*i]) + strlen(headers[2*i + 1]);
	}
	char* buf = malloc(size);

	unsigned offset = 0;
	for(int i = 0; i < amount; i++){
		int name_len = strlen(headers[2*i]);
		int val_len = strlen(headers[2*i+1]);
		memcpy(buf + offset, headers[2*i], name_len);
		memcpy(buf + offset + name_len, headers[2*i+1], val_len);
		lsxpack_header_set_offset2(&hdrs[i], buf + offset, 0, name_len, name_len, val_len);
		offset += name_len + val_len;
	}
	int ret = lsquic_stream_send_headers(stream, &real, 0);

	free(hdrs);
	free(buf);
	return ret;
}


