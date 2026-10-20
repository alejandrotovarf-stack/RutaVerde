package rutaverde.modelo.patrones.factorymethod;

import rutaverde.modelo.entidades.AgenciaRutaVerde;

public class EmisorVoucherAsesor extends EmisorVoucher {

    public EmisorVoucherAsesor(AgenciaRutaVerde agencia) {
        super(agencia);
    }

    @Override
    protected DetalleVoucher crearDetalle() {
        return new DetalleCompleto();
    }
}
