package com.br.firesa.vpn.entity;

import org.springframework.security.core.GrantedAuthority;

import com.br.firesa.vpn.validation.AoAlterar;
import com.br.firesa.vpn.validation.AoInserir;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "vpn_role")
public class Role implements GrantedAuthority {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@NotNull(message = "O código do role deve ser obrigatório", groups = AoAlterar.class)
	@Null(message = "O código do role deve ser nulo", groups = AoInserir.class)
	@EqualsAndHashCode.Include
	private Long id;

	@NotEmpty(message = "A autoridade da role é obrigatória")
	@NotNull(message = "A autoridade da role tem que ser obrigatória")
	@Column(name = "authority")
	private String authority;

	public Role(String authority) {
		this.authority = authority;
	}

}