package lsquicbindings;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

import lsquicbindings.struct.lsquic_engine_api;
import lsquicbindings.struct.lsquic_engine_settings;
import lsquicbindings.struct.lsquic_http_headers;

/**
 * Wrapper for lsquic library <br>
 * TODO: add description from  <a href="https://lsquic.readthedocs.io/en/latest/">lsquic documentation</a> as JavaDoc to methods
 */
public interface LSQuic extends Library {
	
	int lsquic_global_init(int flags);

	void lsquic_global_cleanup();

	void lsquic_set_log_level(String level);
	
	Pointer lsquic_engine_new(int flags, lsquic_engine_api.ByReference api);

	long lsquic_stream_read(Pointer stream, Pointer buf, long buf_size);

	long lsquic_stream_write(Pointer stream, Pointer buf, long buf_size);

	int lsquic_stream_wantwrite(Pointer stream, int want);

	int lsquic_stream_wantread(Pointer stream, int want);

	int lsquic_stream_flush(Pointer stream);

	void lsquic_engine_process_conns(Pointer engine);

	Pointer lsquic_conn_get_ctx(Pointer connection);

	Pointer lsquic_stream_conn(Pointer stream);

	void lsquic_engine_destroy(Pointer engine);

	int lsquic_engine_has_unsent_packets(Pointer engine);
	
	void lsquic_engine_send_unsent_packets(Pointer engine);
	
	int lsquic_engine_packet_in(Pointer engine, byte[] udp_payload, long size, Pointer sa_local, Pointer sa_peer,
			Pointer peer_ctx, int ecn);

	void lsquic_conn_make_stream(Pointer connection);
	
	void lsquic_engine_init_settings(lsquic_engine_settings.ByReference settings, int flags);
	
	Pointer lsquic_engine_connect(Pointer engine, int version, Pointer local_sa, Pointer peer_sa, Pointer peer_ctx,
			Pointer conn_ctx, String sni, short base_plpmtu, Pointer sess_resume, long sess_resume_len, Pointer token,
			long token_sz);

	int lsquic_alpn2ver(String str, long len);
	
	int lsquic_stream_close(Pointer stream);
	
	int lsquic_stream_is_pushed(Pointer stream);
	
	int lsquic_conn_push_stream(Pointer conn, Pointer hdr_set,Pointer stream, lsquic_http_headers.ByReference headers);
	
	int lsquic_stream_send_headers(Pointer stream, Pointer headers, int no_data);
	
	int lsquic_stream_shutdown(Pointer stream, int what);
	
	String lsquic_conn_get_sni(Pointer connection);

	void lsquic_conn_close(Pointer conn);
}
