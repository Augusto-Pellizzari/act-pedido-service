package com.actdigital.lojaonlinepedidobe.infrastructure.repository.impl;

import com.actdigital.lojaonlinepedidobe.domain.model.Pedido;
import com.actdigital.lojaonlinepedidobe.domain.model.StatusPedido;
import com.actdigital.lojaonlinepedidobe.ports.out.PedidoRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

@Repository
public class PedidoRepositoryImpl implements PedidoRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PedidoRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private Pedido map(ResultSet rs, int rowNum) throws java.sql.SQLException {
        Pedido p = new Pedido();
        p.setId(rs.getLong("id"));
        p.setCliente(rs.getString("cliente"));
        p.setStatus(StatusPedido.valueOf(rs.getString("status")));
        p.setDataCriacao(rs.getTimestamp("data_criacao").toLocalDateTime());
        return p;
    }

    @Override
    public void salvar(Pedido pedido) {
        String sql = """
        INSERT INTO pedidos (cliente, status, data_criacao)
        VALUES (:cliente, :status, :data_criacao)
    """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cliente", pedido.getCliente())
                .addValue("status", pedido.getStatus().name())
                .addValue("data_criacao", pedido.getDataCriacao());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(sql, params, keyHolder, new String[] { "id" });

        Number key = keyHolder.getKey();
        if (key != null) {
            pedido.setId(key.longValue());
        }
    }

    @Override
    public List<Pedido> listar() {
        return jdbcTemplate.query("SELECT * FROM pedidos", this::map);
    }

    @Override
    public Optional<Pedido> buscarPorId(Long id) {

        String sql = "SELECT * FROM pedidos WHERE id = :id";

        var params = new MapSqlParameterSource("id", id);
        List<Pedido> list = jdbcTemplate.query(sql, params, this::map);
        return list.stream().findFirst();
    }

    @Override
    public void atualizarStatus(Long id, StatusPedido status) {

        String sql = "UPDATE pedidos SET status = :status WHERE id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("status", status.name());
        jdbcTemplate.update(sql, params);
    }

}
