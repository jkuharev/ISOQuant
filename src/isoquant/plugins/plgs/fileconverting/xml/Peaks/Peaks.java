package isoquant.plugins.plgs.fileconverting.xml.Peaks;

import java.util.HashMap;

/**
 * Created by napedro on 03/02/16.
 */
public class Peaks {

    private String peptide;
    private String logP;
    private String mass;
    private String ppm;
    private String mz;
    private String rt;
    private String scan;
    private String accession;
    private String ptm;

    private String sequence;

    public HashMap<Integer, String> modifications;

    public String getSequence() {
        return sequence;
    }

    public HashMap<Integer, String> getModifications() {
        return modifications;
    }

    public String getPeptide() {
        return peptide;
    }

    public void setPeptide(String peptide) {
        this.peptide = peptide;
        parseModifications();
    }

    public String getLogP() {
        return logP;
    }

    public void setLogP(String logP) {
        this.logP = logP;
    }

    public String getMass() {
        return mass;
    }

    public void setMass(String mass) {
        this.mass = mass;
    }

    public String getPpm() {
        return ppm;
    }

    public void setPpm(String ppm) {
        this.ppm = ppm;
    }

    public String getMz() {
        return mz;
    }

    public void setMz(String mz) {
        this.mz = mz;
    }

    public String getRt() {
        return rt;
    }

    public void setRt(String rt) {
        this.rt = rt;
    }

    public String getScan() {
        return scan;
    }

    public void setScan(String scan) {
        this.scan = scan;
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getPtm() {
        return ptm;
    }

    public void setPtm(String ptm) {
        this.ptm = ptm;
    }

    private void parseModifications(){

        this.modifications = new HashMap<Integer, String>();

        this.sequence = this.peptide.toString();
        int modStartIdx = 0;
        while(this.sequence.contains("(")){
            modStartIdx = this.sequence.indexOf("(", modStartIdx);
            int modEndIdx = this.sequence.indexOf(")", modStartIdx);
            int aaCounter = modStartIdx -1;
            String modif = this.sequence.substring(aaCounter, modEndIdx + 1);
            this.modifications.put(aaCounter + 1, modif);
            this.sequence = sequence.replaceFirst("\\([^A-Za-z]+\\)","");
        }

    }

    @Override
    public String toString(){
        return "Peptide: " + peptide + " mz: " + mz + " RT: " + rt + " accession: " + accession + " PTM: " + ptm + " mods: " + modifications.toString();
    }
}
