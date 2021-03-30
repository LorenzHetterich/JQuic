package lsquicbindings.struct;

import java.util.List;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * Wrapper class for native struct lsquic_engine_api
 */
public class lsquic_engine_api extends Structure {

	public lsquic_engine_settings.ByReference ea_settings;
	public lsquic_stream_if.ByReference ea_stream_if;
	public Pointer ea_stream_if_ctx;
	public lsquic_packets_out_f ea_packets_out;
	public Pointer ea_packets_out_ctx;
	public lsquic_lookup_cert_f ea_lookup_cert;
	public Pointer ea_cert_lu_ctx;
	public ea_get_ssl_ctx ea_get_ssl_ctx;
	public Pointer ea_shi;
	public Pointer ea_shi_ctx;
	public Pointer ea_pmi;
	public Pointer ea_pmi_ctx;
	public Pointer ea_new_scids;
	public Pointer ea_live_scids;
	public Pointer ea_old_scids;
	public Pointer ea_cids_update_ctx;
	public Pointer ea_verify_cert;
	public Pointer ea_verify_ctx;
	public Pointer ea_hsi_if;
	public Pointer ea_hsi_ctx;
	public Pointer ea_stats_fh;
	public String ea_alpn;
	public Pointer ea_generate_scid;
	
	
	@Override
	protected List<String> getFieldOrder() {
		return List.of(
			"ea_settings",
			"ea_stream_if",
			"ea_stream_if_ctx",
			"ea_packets_out",
			"ea_packets_out_ctx",
			"ea_lookup_cert",
			"ea_cert_lu_ctx",
			"ea_get_ssl_ctx",
			"ea_shi",
			"ea_shi_ctx",
			"ea_pmi",
			"ea_pmi_ctx",
			"ea_new_scids",
			"ea_live_scids",
			"ea_old_scids",
			"ea_cids_update_ctx",
			"ea_verify_cert",
			"ea_verify_ctx",
			"ea_hsi_if",
			"ea_hsi_ctx",
			"ea_stats_fh",
			"ea_alpn",
			"ea_generate_scid"
		);
	}

	public static class ByReference extends lsquic_engine_api implements Structure.ByReference {
		
	}
	
	@FunctionalInterface
	public static interface lsquic_packets_out_f extends Callback {
		int invoke(Pointer packets_out_ctx, lsquic_out_spec.ByReference out_spec, int n_packets_out);
	}
	
	@FunctionalInterface
	public static interface ea_get_ssl_ctx extends Callback {
		Pointer invoke(Pointer peer_ctx, Pointer local_addr);
	}

	@FunctionalInterface
	public static interface lsquic_lookup_cert_f extends Callback {
		Pointer invoke(Pointer lsquic_cert_lookup_ctx, Pointer local, Pointer sni);
	}
	
}
