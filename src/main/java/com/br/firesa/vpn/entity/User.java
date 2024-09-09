package com.br.firesa.vpn.entity;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.br.firesa.vpn.security.CryptoConverter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true, value = { "hibernateLazyInitializer", "handler" })
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "vpn_user")
public class User implements UserDetails {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@Column(unique = true, name = "username")
	@NotEmpty(message = "O nome do Usuário é obrigatório")
	@NotNull(message = "O nome do Usuário tem que ser obrigatório")
	private String username;

	@NotEmpty(message = "A senha do Usuário é obrigatória")
	@NotNull(message = "A senha do Usuário tem que ser obrigatória")
	@Column(name = "password")
	private String password;

	@Convert(converter = CryptoConverter.class)
	@Column(name = "userPublicKey", columnDefinition = "bytea")
	private byte[] userPublicKey;

	@Convert(converter = CryptoConverter.class)
	@Column(name = "serverPrivateKey", columnDefinition = "bytea")
	private byte[] serverPrivateKey;

	@Convert(converter = CryptoConverter.class)
	@Column(name = "serverPublicKey", columnDefinition = "bytea")
	private byte[] serverPublicKey;

	@JsonIgnore
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "AppUserRoles", joinColumns = { @JoinColumn(name = "appUser_id") }, inverseJoinColumns = {
			@JoinColumn(name = "role_id") })
	private Set<Role> authorities;

	public User(String username, String password, Set<Role> authorities) {
		this.username = username;
		this.password = password;
		this.authorities = authorities;
	}

	public User(String username, String password, byte[] userPublicKey) {
		super();
		this.username = username;
		this.password = password;
		this.userPublicKey = userPublicKey;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	@JsonIgnore // Ignora o password na serialização (resposta)
	public String getPassword() {
		return password;
	}

	@JsonProperty // Permite a desserialização (receber na requisição)
	public void setPassword(String password) {
		this.password = password;
	}

}
