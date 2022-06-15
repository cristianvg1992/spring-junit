package com.cvg.mockito.test;

import com.cvg.mockito.test.exceptions.DineroInsuficienteException;
import com.cvg.mockito.test.models.Banco;
import com.cvg.mockito.test.models.Cuenta;
import com.cvg.mockito.test.repository.BancoRepository;
import com.cvg.mockito.test.repository.CuentaRepository;
import com.cvg.mockito.test.service.CuentaService;
import com.cvg.mockito.test.service.CuentaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class SpringTestApplicationTests {
//	@Mock CuentaRepository cuentaRepository;
 	@MockBean CuentaRepository cuentaRepository;
//	@Mock BancoRepository bancoRepository;
	@MockBean BancoRepository bancoRepository;
//	@InjectMocks CuentaServiceImpl cuentaService;
	@Autowired CuentaService cuentaService;

	@Test
	void testSaldoCuenta_transfer() {
		// GIVEN
		when(cuentaRepository.findById(1L)).thenReturn( Datos.CUENTA_001() );
		when(cuentaRepository.findById(2L)).thenReturn( Datos.CUENTA_002() );
		when(bancoRepository.findById(1L)).thenReturn( Datos.BANCO_001() );

		// WHEN
		BigDecimal saldoOrigen = cuentaService.revisarSaldo(1L);
		BigDecimal saldoDestino = cuentaService.revisarSaldo(2L);
		assertEquals("21000", saldoOrigen.toPlainString());
		assertEquals( "11000", saldoDestino.toPlainString() );

		cuentaService.transferirSaldo( 1L, 2L, new BigDecimal("100"), 1L );

		saldoOrigen = cuentaService.revisarSaldo(1L);
		saldoDestino = cuentaService.revisarSaldo(2L);
		assertEquals( "20900", saldoOrigen.toPlainString() );
		assertEquals( "11100", saldoDestino.toPlainString() );

		Integer totalTransferencia = cuentaService.revisarTotalTransferencia(1L);
		assertEquals(1, totalTransferencia);

		verify( cuentaRepository, times(3) ).findById(1L);
		verify( cuentaRepository, times(3) ).findById(2L);

		verify( cuentaRepository, times(2) ).update( any(Cuenta.class) );
		verify( bancoRepository, times(2) ).findById(1L);
		verify( bancoRepository ).update(any(Banco.class));

		verify( cuentaRepository, times(6) ).findById( anyLong() );
		verify( cuentaRepository, never() ).findAll();
	}

	@Test
	void testSaldoInsuficienteExceptions() {
		// GIVEN
		when(cuentaRepository.findById(1L)).thenReturn( Datos.CUENTA_001() );
		when(cuentaRepository.findById(2L)).thenReturn( Datos.CUENTA_002() );
		when(bancoRepository.findById(1L)).thenReturn( Datos.BANCO_001() );

		// WHEN
		BigDecimal saldoOrigen = cuentaService.revisarSaldo(1L);
		BigDecimal saldoDestino = cuentaService.revisarSaldo(2L);
		assertEquals("21000", saldoOrigen.toPlainString());
		assertEquals( "11000", saldoDestino.toPlainString() );

		Exception exception = assertThrows(DineroInsuficienteException.class, () -> {
			cuentaService.transferirSaldo( 1L, 2L, new BigDecimal("22000"), 1L );
		});


		saldoOrigen = cuentaService.revisarSaldo(1L);
		saldoDestino = cuentaService.revisarSaldo(2L);
		assertEquals( "21000", saldoOrigen.toPlainString() );
		assertEquals( "11000", saldoDestino.toPlainString() );

		Integer totalTransferencia = cuentaService.revisarTotalTransferencia(1L);
		assertEquals(0, totalTransferencia);

		verify( cuentaRepository, times(3) ).findById(1L);
		verify( cuentaRepository, times(2) ).findById(2L);

		verify( cuentaRepository, never() ).update( any(Cuenta.class) );
		verify( bancoRepository, times(1) ).findById(1L);
		verify( bancoRepository, never() ).update(any(Banco.class));

		verify( cuentaRepository, times(5) ).findById( anyLong() );
		verify( cuentaRepository, never() ).findAll();
	}

	@Test
	void test_ifItsTheSame_thenReturnTrue() {
		when( cuentaRepository.findById(1L) ).thenReturn( Datos.CUENTA_001() );
		Cuenta cuenta1 = cuentaService.findById(1L);
		Cuenta cuenta2 = cuentaService.findById(1L);

		assertSame( cuenta1, cuenta2 );
		assertEquals( "CRISTHIAN", cuenta1.getPersona() );
		assertEquals( "CRISTHIAN", cuenta2.getPersona() );

		verify( cuentaRepository, times(2) ).findById(1L);
	}
}
