package lsquicbindings.struct;

import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * Wrapper class for native struct iovec
 */
public class lsquic_engine_settings extends Structure {

	/**
	 * This is a bit mask wherein each bit corresponds to a value in enum
	 * lsquic_version. Client starts negotiating with the highest version and goes
	 * down. Server supports either of the versions specified here.
	 *
	 * This setting applies to both Google and IETF QUIC.
	 *
	 * @see lsquic_version
	 */
	public int es_versions;

	/**
	 * Initial default CFCW.
	 *
	 * In server mode, per-connection values may be set lower than this if resources
	 * are scarce.
	 *
	 * Do not set es_cfcw and es_sfcw lower than @ref LSQUIC_MIN_FCW.
	 *
	 * @see es_max_cfcw
	 */
	public int es_cfcw;

	/**
	 * Initial default SFCW.
	 *
	 * In server mode, per-connection values may be set lower than this if resources
	 * are scarce.
	 *
	 * Do not set es_cfcw and es_sfcw lower than @ref LSQUIC_MIN_FCW.
	 *
	 * @see es_max_sfcw
	 */
	public int es_sfcw;

	/**
	 * This value is used to specify maximum allowed value CFCW is allowed to reach
	 * due to window auto-tuning. By default, this value is zero, which means that
	 * CFCW is not allowed to increase from its initial value.
	 *
	 * This setting is applicable to both gQUIC and IETF QUIC.
	 *
	 * @see es_cfcw, @see es_init_max_data.
	 */
	public int es_max_cfcw;

	/**
	 * This value is used to specify the maximum value stream flow control window is
	 * allowed to reach due to auto-tuning. By default, this value is zero, meaning
	 * that auto-tuning is turned off.
	 *
	 * This setting is applicable to both gQUIC and IETF QUIC.
	 *
	 * @see es_sfcw, @see es_init_max_stream_data_bidi_remote,
	 * @see es_init_max_stream_data_bidi_local.
	 */
	public int es_max_sfcw;

	/** MIDS */
	public int es_max_streams_in;

	/**
	 * Handshake timeout in microseconds.
	 *
	 * For client, this can be set to an arbitrary value (zero turns the timeout
	 * off).
	 *
	 * For server, this value is limited to about 16 seconds. Do not set it to zero.
	 */
	public long es_handshake_to;

	/** ICSL in microseconds; GQUIC only */
	public long es_idle_conn_to;

	/**
	 * When true, CONNECTION_CLOSE is not sent when connection times out. The server
	 * will also not send a reply to client's CONNECTION_CLOSE.
	 *
	 * Corresponds to SCLS (silent close) gQUIC option.
	 */
	public int es_silent_close;

	/**
	 * This corresponds to SETTINGS_MAX_HEADER_LIST_SIZE (RFC 7540, Section 6.5.2).
	 * 0 means no limit. Defaults to @ref LSQUIC_DF_MAX_HEADER_LIST_SIZE.
	 */
	public int es_max_header_list_size;

	/** UAID -- User-Agent ID. Defaults to @ref LSQUIC_DF_UA. */
	public Pointer es_ua;

	/**
	 * More parameters for server
	 */
	public long es_sttl; /* SCFG TTL in seconds */

	public int es_pdmd; /* One fixed value X509 */
	public int es_aead; /* One fixed value AESG */
	public int es_kexs; /* One fixed value C255 */

	/*
	 * Maximum number of incoming connections in inchoate state. This is only
	 * applicable in server mode.
	 */
	public int es_max_inchoate;

	/**
	 * Setting this value to 0 means that
	 *
	 * For client: a) we send a SETTINGS frame to indicate that we do not support
	 * server push; and b) All incoming pushed streams get reset immediately. (For
	 * maximum effect, set es_max_streams_in to 0.)
	 *
	 * For server: lsquic_conn_push_stream() will return -1.
	 */
	public int es_support_push;

	/**
	 * If set to true value, the server will not include connection ID in outgoing
	 * packets if client's CHLO specifies TCID=0.
	 *
	 * For client, this means including TCID=0 public into CHLO message. Note that
	 * in this case, the engine tracks connections by the (source-addr, dest-addr)
	 * tuple, thereby making it necessary to create a socket for each connection.
	 *
	 * This option has no effect in Q046 and Q050, as the server never includes CIDs
	 * in the public short packets.
	 *
	 * This setting is applicable to gQUIC only.
	 *
	 * The default is @ref LSQUIC_DF_SUPPORT_TCID0.
	 */
	public int es_support_tcid0;

	/**
	 * Q037 and higher support "No STOP_WAITING frame" mode. When set, the client
	 * will send NSTP option in its Client Hello message and will not sent
	 * STOP_WAITING frames, while ignoring incoming STOP_WAITING frames, if any.
	 * Note that if the version negotiation happens to downgrade the client below
	 * Q037, this mode will *not* be used.
	 *
	 * This option does not affect the server, as it must support NSTP mode if it
	 * was specified by the client.
	 *
	 * This setting is applicable to gQUIC only.
	 */
	public int es_support_nstp;

	/**
	 * If set to true value, the library will drop connections when it receives
	 * corresponding Public Reset packet. The default is to ignore these packets.
	 *
	 * The default is @ref LSQUIC_DF_HONOR_PRST.
	 */
	public int es_honor_prst;

	/**
	 * If set to true value, the library will send Public Reset packets in response
	 * to incoming packets with unknown Connection IDs. The default is @ref
	 * LSQUIC_DF_SEND_PRST.
	 */
	public int es_send_prst;

	/**
	 * A non-zero value enables public internal checks that identify suspected
	 * infinite loops in user @ref on_read and @ref on_write callbacks and break
	 * them. An infinite loop may occur if user code keeps on performing the same
	 * operation without checking status, e.g. reading from a closed stream etc.
	 *
	 * The value of this parameter is as follows: should a callback return this
	 * number of times in a row without making progress (that is, reading, writing,
	 * or changing stream state), loop break will occur.
	 *
	 * The defaut value is @ref LSQUIC_DF_PROGRESS_CHECK.
	 */
	public int es_progress_check;

	/**
	 * A non-zero value make stream dispatch its read-write events once per call.
	 *
	 * When zero, read and write events are dispatched until the stream is no longer
	 * readable or writeable, respectively, or until the user signals unwillingness
	 * to read or write using
	 * 
	 * @ref lsquic_stream_wantread() or @ref lsquic_stream_wantwrite() or shuts down
	 *      the stream.
	 *
	 *      This also applies to the on_dg_write() callback.
	 *
	 *      The default value is @ref LSQUIC_DF_RW_ONCE.
	 */
	public int es_rw_once;

	/**
	 * If set, this value specifies the number of microseconds that
	 * 
	 * @ref lsquic_engine_process_conns() and
	 * @ref lsquic_engine_send_unsent_packets() are allowed to spend before
	 *      returning.
	 *
	 *      This is not an exact science and the connections must make progress, so
	 *      the deadline is checked after all connections get a chance to tick (in
	 *      the case of @ref lsquic_engine_process_conns()) and at least one batch
	 *      of packets is sent out.
	 *
	 *      When processing function runs out of its time slice, immediate calls
	 *      to @ref lsquic_engine_has_unsent_packets() return false.
	 *
	 *      The default value is @ref LSQUIC_DF_PROC_TIME_THRESH.
	 */
	public int es_proc_time_thresh;

	/**
	 * If set to true, packet pacing is implemented per connection.
	 *
	 * The default value is @ref LSQUIC_DF_PACE_PACKETS.
	 */
	public int es_pace_packets;

	/**
	 * Clock granularity information is used by the pacer. The value is in
	 * microseconds; default is @ref LSQUIC_DF_CLOCK_GRANULARITY.
	 */
	public int es_clock_granularity;

	/**
	 * Congestion control algorithm to use.
	 *
	 * 0: Use default (@ref LSQUIC_DF_CC_ALGO) 1: Cubic 2: BBRv1 3: Adaptive (Cubic
	 * or BBRv1)
	 */
	public int es_cc_algo;

	/**
	 * Congestion controller RTT threshold in microseconds.
	 *
	 * Adaptive congestion control uses BBRv1 until RTT is determined. At that
	 * popublic int a permanent choice of congestion controller is made. If RTT is
	 * smaller than or equal to es_cc_rtt_thresh, congestion controller is switched
	 * to Cubic; otherwise, BBRv1 is picked.
	 *
	 * The default value is @ref LSQUIC_DF_CC_RTT_THRESH.
	 */
	public int es_cc_rtt_thresh;

	/**
	 * No progress timeout.
	 *
	 * If connection does not make progress for this number of seconds, the
	 * connection is dropped. Here, progress is defined as user streams being
	 * written to or read from.
	 *
	 * If this value is zero, this timeout is disabled.
	 *
	 * Default value is @ref LSQUIC_DF_NOPROGRESS_TIMEOUT_SERVER in server mode
	 * and @ref LSQUIC_DF_NOPROGRESS_TIMEOUT_CLIENT in client mode.
	 */
	public int es_noprogress_timeout;

	/* The following settings are specific to IETF QUIC. */
	/* vvvvvvvvvvv */

	/**
	 * Initial max data.
	 *
	 * This is a transport parameter.
	 *
	 * Depending on the engine mode, the default value is either
	 * 
	 * @ref LSQUIC_DF_INIT_MAX_DATA_CLIENT or
	 * @ref LSQUIC_DF_INIT_MAX_DATA_SERVER.
	 */
	public int es_init_max_data;

	/**
	 * Initial maximum amount of stream data allowed to be sent on streams created
	 * by remote end (peer).
	 *
	 * This is a transport parameter.
	 *
	 * Depending on the engine mode, the default value is either
	 * 
	 * @ref LSQUIC_DF_INIT_MAX_STREAM_DATA_BIDI_REMOTE_CLIENT or
	 * @ref LSQUIC_DF_INIT_MAX_STREAM_DATA_BIDI_REMOTE_SERVER.
	 */
	public int es_init_max_stream_data_bidi_remote;

	/**
	 * Initial maximum amount of stream data allowed to be sent on streams created
	 * by remote end (peer).
	 *
	 * This is a transport parameter.
	 *
	 * Depending on the engine mode, the default value is either
	 * 
	 * @ref LSQUIC_DF_INIT_MAX_STREAM_DATA_BIDI_LOCAL_CLIENT or
	 * @ref LSQUIC_DF_INIT_MAX_STREAM_DATA_BIDI_LOCAL_SERVER.
	 */
	public int es_init_max_stream_data_bidi_local;

	/**
	 * Initial max stream data for unidirectional streams initiated by remote
	 * endpopublic int.
	 *
	 * This is a transport parameter.
	 *
	 * Depending on the engine mode, the default value is either
	 * 
	 * @ref LSQUIC_DF_INIT_MAX_STREAM_DATA_UNI_CLIENT or
	 * @ref LSQUIC_DF_INIT_MAX_STREAM_DATA_UNI_SERVER.
	 */
	public int es_init_max_stream_data_uni;

	/**
	 * Maximum initial number of bidirectional stream.
	 *
	 * This is a transport parameter.
	 *
	 * Default value is @ref LSQUIC_DF_INIT_MAX_STREAMS_BIDI.
	 */
	public int es_init_max_streams_bidi;

	/**
	 * Maximum initial number of unidirectional stream.
	 *
	 * This is a transport parameter.
	 *
	 * Default value is @ref LSQUIC_DF_INIT_MAX_STREAMS_UNI_CLIENT or
	 * 
	 * @ref LSQUIC_DF_INIT_MAX_STREAM_DATA_UNI_SERVER.
	 */
	public int es_init_max_streams_uni;

	/**
	 * Idle connection timeout.
	 *
	 * This is a transport parameter.
	 *
	 * (Note: es_idle_conn_to is not reused because it is in microseconds, which, I
	 * now realize, was not a good choice. Since it will be obsoleted some time
	 * after the switchover to IETF QUIC, we do not have to keep on using strange
	 * units.)
	 *
	 * Default value is @ref LSQUIC_DF_IDLE_TIMEOUT.
	 *
	 * Maximum value is 600 seconds.
	 */
	public int es_idle_timeout;

	/**
	 * Ping period. If set to non-zero value, the connection will generate and send
	 * PING frames in the absence of other activity.
	 *
	 * By default, the server does not send PINGs and the period is set to zero. The
	 * client's defaut value is @ref LSQUIC_DF_PING_PERIOD.
	 */
	public int es_ping_period;

	/**
	 * Source Connection ID length. Only applicable to the IETF QUIC versions. Valid
	 * values are 0 through 20, inclusive.
	 *
	 * Default value is @ref LSQUIC_DF_SCID_LEN.
	 */
	public int es_scid_len;

	/**
	 * Source Connection ID issuance rate. Only applicable to the IETF QUIC
	 * versions. This field is measured in CIDs per minute. Using value 0 indicates
	 * that there is no rate limit for CID issuance.
	 *
	 * Default value is @ref LSQUIC_DF_SCID_ISS_RATE.
	 */
	public int es_scid_iss_rate;

	/**
	 * Maximum size of the QPACK dynamic table that the QPACK decoder will use.
	 *
	 * The default is @ref LSQUIC_DF_QPACK_DEC_MAX_SIZE.
	 */
	public int es_qpack_dec_max_size;

	/**
	 * Maximum number of blocked streams that the QPACK decoder is willing to
	 * tolerate.
	 *
	 * The default is @ref LSQUIC_DF_QPACK_DEC_MAX_BLOCKED.
	 */
	public int es_qpack_dec_max_blocked;

	/**
	 * Maximum size of the dynamic table that the encoder is willing to use. The
	 * actual size of the dynamic table will not exceed the minimum of this value
	 * and the value advertized by peer.
	 *
	 * The default is @ref LSQUIC_DF_QPACK_ENC_MAX_SIZE.
	 */
	public int es_qpack_enc_max_size;

	/**
	 * Maximum number of blocked streams that the QPACK encoder is willing to risk.
	 * The actual number of blocked streams will not exceed the minimum of this
	 * value and the value advertized by peer.
	 *
	 * The default is @ref LSQUIC_DF_QPACK_ENC_MAX_BLOCKED.
	 */
	public int es_qpack_enc_max_blocked;

	/**
	 * Enable ECN support.
	 *
	 * The default is @ref LSQUIC_DF_ECN
	 */
	public int es_ecn;

	/**
	 * Allow peer to migrate connection.
	 *
	 * The default is @ref LSQUIC_DF_ALLOW_MIGRATION
	 */
	public int es_allow_migration;

	/**
	 * Use QL loss bits. Allowed values are: 0: Do not use loss bits 1: Allow loss
	 * bits 2: Allow and send loss bits
	 *
	 * Default value is @ref LSQUIC_DF_QL_BITS
	 */
	public int es_ql_bits;

	/**
	 * Enable spin bit. Allowed values are 0 and 1.
	 *
	 * Default value is @ref LSQUIC_DF_SPIN
	 */
	public int es_spin;

	/**
	 * Enable delayed ACKs extension. Allowed values are 0 and 1.
	 *
	 * Default value is @ref LSQUIC_DF_DELAYED_ACKS
	 */
	public int es_delayed_acks;

	/**
	 * Enable timestamps extension. Allowed values are 0 and 1.
	 *
	 * Default value is @ref LSQUIC_DF_TIMESTAMPS
	 */
	public int es_timestamps;

	/**
	 * Maximum packet size we are willing to receive. This is sent to peer in
	 * transport parameters: the library does not enforce this limit for incoming
	 * packets.
	 *
	 * If set to zero, limit is not set.
	 *
	 * Default value is @ref LSQUIC_DF_MAX_UDP_PAYLOAD_SIZE_RX
	 */
	public short es_max_udp_payload_size_rx;

	/**
	 * Enable the "QUIC bit grease" extension. When set to a true value, lsquic will
	 * grease the QUIC bit on the outgoing QUIC packets if the peer sent the
	 * "grease_quic_bit" transport parameter.
	 *
	 * Default value is @ref LSQUIC_DF_GREASE_QUIC_BIT
	 */
	public int es_grease_quic_bit;

	/**
	 * If set to true value, enable DPLPMTUD -- Datagram Packetization Layer Path
	 * MTU Discovery.
	 *
	 * Default value is @ref LSQUIC_DF_DPLPMTUD
	 */
	public int es_dplpmtud;

	/**
	 * PLPMTU size expected to work for most paths.
	 *
	 * If set to zero, this value is calculated based on QUIC and IP versions.
	 *
	 * Default value is @ref LSQUIC_DF_BASE_PLPMTU.
	 */
	public short es_base_plpmtu;

	/**
	 * Largest PLPMTU size the engine will try.
	 *
	 * If set to zero, picking this value is left to the engine.
	 *
	 * Default value is @ref LSQUIC_DF_MAX_PLPMTU.
	 */
	public short es_max_plpmtu;

	/**
	 * This value specifies how public long the DPLPMTUD probe timer is, in
	 * milliseconds. [draft-ietf-tsvwg-datagram-plpmtud-17] says:
	 *
	 * " PROBE_TIMER: The PROBE_TIMER is configured to expire after a period "
	 * longer than the maximum time to receive an acknowledgment to a " probe
	 * packet. This value MUST NOT be smaller than 1 second, and " SHOULD be larger
	 * than 15 seconds. Guidance on selection of the " timer value are provided in
	 * section 3.1.1 of the UDP Usage " Guidelines [RFC8085].
	 *
	 * If set to zero, the default is used.
	 *
	 * Default value is @ref LSQUIC_DF_MTU_PROBE_TIMER.
	 */
	public int es_mtu_probe_timer;

	/**
	 * Enable datagram extension. Allowed values are 0 and 1.
	 *
	 * Default value is @ref LSQUIC_DF_DATAGRAMS
	 */
	public int es_datagrams;

	/**
	 * If set to true, changes in peer port are assumed to be due to a benign NAT
	 * rebinding and path characteristics -- MTU, RTT, and CC state -- are not
	 * reset.
	 *
	 * Default value is @ref LSQUIC_DF_OPTIMISTIC_NAT.
	 */
	public int es_optimistic_nat;

	/**
	 * If set to true, Extensible HTTP Priorities are enabled. This is HTTP/3-only
	 * setting.
	 *
	 * Default value is @ref LSQUIC_DF_EXT_HTTP_PRIO
	 */
	public int es_ext_http_prio;

	/**
	 * If set to 1, QPACK statistics are logged per connection.
	 *
	 * If set to 2, QPACK experiments are run. In this mode, encoder and decoder
	 * setting values are randomly selected (from the range [0, whatever is
	 * specified in es_qpack_(enc|dec)_*]) and these values apublic long with
	 * compression ratio and user agent are logged at NOTICE level when connection
	 * is destroyed. The purpose of these experiments is to use compression
	 * performance statistics to figure out a good set of default values.
	 *
	 * Default value is @ref LSQUIC_DF_QPACK_EXPERIMENT.
	 */
	public int es_qpack_experiment;

	/**
	 * Settings for the Packet Tolerance PID Controller (PTPC) used for the Delayed
	 * ACKs logic. Periodicity is how often the number of incoming ACKs is sampled.
	 * Periodicity's units is the number of RTTs. Target is the average number of
	 * incoming ACKs per RTT we want to achieve. Error threshold defines the range
	 * of error values within which no action is taken. For example, error threshold
	 * of 0.03 means that adjustment actions will be taken only when the error is
	 * outside of the [-0.03, 0.03] range. Proportional and public integral gains
	 * have their usual meanings described here:
	 * https://en.wikipedia.org/wiki/PID_controller#Controller_theory
	 *
	 * The average is normalized as follows: AvgNormalized = Avg * e / Target #
	 * Where 'e' is 2.71828...
	 *
	 * The error is then calculated as ln(AvgNormalized) - 1. This gives us a
	 * logarithmic scale that is convenient to use for adjustment calculations. The
	 * error divisor is used to calculate the packet tolerance adjustment:
	 * Adjustment = Error / ErrorDivisor
	 *
	 * WARNING. The library comes with sane defaults. Only fiddle with these knobs
	 * if you know what you are doing.
	 */
	public int es_ptpc_periodicity; /* LSQUIC_DF_PTPC_PERIODICITY */
	public int es_ptpc_max_packtol; /* LSQUIC_DF_PTPC_MAX_PACKTOL */
	public int es_ptpc_dyn_target; /* LSQUIC_DF_PTPC_DYN_TARGET */
	public float es_ptpc_target, /* LSQUIC_DF_PTPC_TARGET */
			es_ptpc_prop_gain, /* LSQUIC_DF_PTPC_PROP_GAIN */
			es_ptpc_int_gain, /* LSQUIC_DF_PTPC_public int_GAIN */
			es_ptpc_err_thresh, /* LSQUIC_DF_PTPC_ERR_THRESH */
			es_ptpc_err_divisor; /* LSQUIC_DF_PTPC_ERR_DIVISOR */

	/**
	 * When set to true, the on_close() callback will be delayed until the peer
	 * acknowledges all data sent on the stream. (Or until the connection is
	 * destroyed in some manner -- either explicitly closed by the user or as a
	 * result of an engine shutdown.)
	 *
	 * Default value is @ref LSQUIC_DF_DELAY_ONCLOSE
	 */
	public int es_delay_onclose;

	/**
	 * If set to a non-zero value, specified maximum batch size. (The batch of
	 * packets passed to @ref ea_packets_out() callback). Must be no larger than
	 * 1024.
	 *
	 * Default value is @ref LSQUIC_DF_MAX_BATCH_SIZE
	 */
	public int es_max_batch_size;

	/**
	 * When true, sanity checks are performed on peer's transport parameter values.
	 * If some limits are set suspiciously low, the connection won't be established.
	 *
	 * Default value is @ref LSQUIC_DF_CHECK_TP_SANITY
	 */
	public int es_check_tp_sanity;

	@Override
	protected List<String> getFieldOrder() {
		return List.of("es_versions", "es_cfcw", "es_sfcw", "es_max_cfcw", "es_max_sfcw", "es_max_streams_in",
				"es_handshake_to", "es_idle_conn_to", "es_silent_close", "es_max_header_list_size", "es_ua", "es_sttl",
				"es_pdmd", "es_aead", "es_kexs", "es_max_inchoate", "es_support_push", "es_support_tcid0",
				"es_support_nstp", "es_honor_prst", "es_send_prst", "es_progress_check", "es_rw_once",
				"es_proc_time_thresh", "es_pace_packets", "es_clock_granularity", "es_cc_algo", "es_cc_rtt_thresh",
				"es_noprogress_timeout", "es_init_max_data", "es_init_max_stream_data_bidi_remote",
				"es_init_max_stream_data_bidi_local", "es_init_max_stream_data_uni", "es_init_max_streams_bidi",
				"es_init_max_streams_uni", "es_idle_timeout", "es_ping_period", "es_scid_len", "es_scid_iss_rate",
				"es_qpack_dec_max_size", "es_qpack_dec_max_blocked", "es_qpack_enc_max_size",
				"es_qpack_enc_max_blocked", "es_ecn", "es_allow_migration", "es_ql_bits", "es_spin", "es_delayed_acks",
				"es_timestamps", "es_max_udp_payload_size_rx", "es_grease_quic_bit", "es_dplpmtud", "es_base_plpmtu",
				"es_max_plpmtu", "es_mtu_probe_timer", "es_datagrams", "es_optimistic_nat", "es_ext_http_prio",
				"es_qpack_experiment", "es_ptpc_periodicity", "es_ptpc_max_packtol", "es_ptpc_dyn_target",
				"es_ptpc_target", "es_ptpc_prop_gain", "es_ptpc_int_gain", "es_ptpc_err_thresh", "es_ptpc_err_divisor",
				"es_delay_onclose", "es_max_batch_size", "es_check_tp_sanity");
	}

	public static class ByReference extends lsquic_engine_settings implements Structure.ByReference {

	}

}
