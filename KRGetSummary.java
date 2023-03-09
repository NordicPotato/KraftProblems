package org.kraft.entreg.delivery.process;

import java.net.URL;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MPInstance;
import org.compiere.model.MProcess;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfo;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Trx;

import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

public class KRGetSummary {
	public JasperPrint generateSummaryJasperPrint(Properties ctx, List<Integer> ordersList, String trxName) {
		ProcessInfo pi = new ProcessInfo("", 0, 0, 0);
		MProcess pr = new Query(Env.getCtx(), MProcess.Table_Name, "value=?", null).setParameters(new Object[] { "KR_GenerateDeliverySummary" }).first();
		
		ProcGenerateSummary proc = new ProcGenerateSummary();
		proc.setOrdersList(ordersList);
		MPInstance mpi = new MPInstance(ctx, 0, null);
		mpi.setAD_Process_ID(pr.get_ID());
		mpi.setRecord_ID(0);
		mpi.save();
		pi.setAD_PInstance_ID(mpi.get_ID());

		if (!proc.startProcess(Env.getCtx(), pi, trxName != null ? Trx.get(trxName, false) : null))
			throw new AdempiereException("Could not generate delivery summary records");
		
		// Create JasperPrint
		try {
			URL path = ProcPrintDelivery.class.getClassLoader()
				.getResource("org/kraft/entreg/delivery/report/resumoentrega.jasper");

			HashMap<String, Object> param = new HashMap<String, Object>();
			param.put("AD_PInstance_ID", proc.getProcessInfo().getAD_PInstance_ID());

			// connection
			Connection con = DB.getConnectionRO();

			//
			JasperPrint jasperPrint = JasperFillManager.fillReport(path.openStream(), param, con);

			// close
			con.close();
			return jasperPrint;

		} catch (Exception ex) {
			throw new AdempiereException(ex);
		}
	}
}
