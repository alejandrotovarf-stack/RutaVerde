package rutaverde.modelo.patrones.factorymethod;

import rutaverde.modelo.entidades.AgenciaRutaVerde;

public class EmisorVoucherKiosco extends EmisorVoucher {

    public EmisorVoucherKiosco(AgenciaRutaVerde agencia) {
        super(agencia);
    }

    @Override
    protected DetalleVoucher crearDetalle() {
        return new DetalleTiquete();
    }
}
