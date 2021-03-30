package lsquicbindings.struct;

import java.util.List;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * Wrapper class for native struct lsquic_stream_if
 */
public class lsquic_stream_if extends Structure {

	public on_new_conn on_new_conn;
	public on_goaway_received on_goaway_received;
	public on_conn_closed on_conn_closed;
	public on_new_stream on_new_stream;
	public on_read on_read;
	public on_write on_write;
	public on_close on_close;
	public on_dg_write on_dg_write;
	public on_datagram on_datagram;
	public on_hsk_done on_hsk_done;
	public on_new_token on_new_token;
	public on_sess_resume_info on_sess_resume_info;
	public on_reset on_reset;
	public on_conncloseframe_received on_conncloseframe_received;

	
	@Override
	protected List<String> getFieldOrder() {
		return List.of("on_new_conn", "on_goaway_received", "on_conn_closed", "on_new_stream", "on_read", "on_write",
				"on_close", "on_dg_write", "on_datagram", "on_hsk_done", "on_new_token", "on_sess_resume_info",
				"on_reset", "on_conncloseframe_received");
	}

	public static class ByReference extends lsquic_stream_if implements Structure.ByReference {

	}

	/*
	 * Functional interfaces for callbacks
	 */

	@FunctionalInterface
	public static interface on_new_conn extends Callback {

		Pointer invoke(Pointer conn_if_ctx, Pointer connection);

	}

	@FunctionalInterface
	public static interface on_conn_closed extends Callback {

		void invoke(Pointer connection);

	}

	@FunctionalInterface
	public static interface on_new_stream extends Callback {

		Pointer invoke(Pointer stream_if_ctx, Pointer stream);

	}

	@FunctionalInterface
	public static interface on_read extends Callback {

		void invoke(Pointer stream, Pointer context);

	}

	@FunctionalInterface
	public static interface on_write extends Callback {

		void invoke(Pointer stream, Pointer context);

	}

	@FunctionalInterface
	public static interface on_close extends Callback {

		void invoke(Pointer stream, Pointer context);

	}

	@FunctionalInterface
	public static interface on_reset extends Callback {

		void invoke(Pointer stream, Pointer context, int how);

	}

	@FunctionalInterface
	public static interface on_hsk_done extends Callback {

		void invoke(Pointer connection, Pointer handshake_status);

	}

	@FunctionalInterface
	public static interface on_goaway_received extends Callback {

		void invoke(Pointer connection);

	}

	@FunctionalInterface
	public static interface on_new_token extends Callback {

		void invoke(Pointer connection, Pointer token, long token_size);

	}

	@FunctionalInterface
	public static interface on_sess_resume_info extends Callback {

		void invoke(Pointer connection, Pointer info, long info_size);

	}

	@FunctionalInterface
	public static interface on_dg_write extends Callback {

		long invoke(Pointer connection, Pointer buf, long buf_size);

	}

	@FunctionalInterface
	public static interface on_datagram extends Callback {

		void invoke(Pointer connection, Pointer buf, long buf_size);

	}

	@FunctionalInterface
	public static interface on_conncloseframe_received extends Callback {

		void invoke(Pointer connection, int app_error, long error_code, Pointer reason, int reason_len);

	}

}
