package com.actdigital.lojaonlinepedidobe.infrastructure.repository.impl;

import com.actdigital.lojaonlinepedidobe.domain.model.Pedido;
import com.actdigital.lojaonlinepedidobe.domain.model.StatusPedido;
import com.actdigital.lojaonlinepedidobe.infrastructure.exception.CustomException;
import com.actdigital.lojaonlinepedidobe.infrastructure.exception.ErrorCode;
import com.actdigital.lojaonlinepedidobe.ports.out.PedidoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

@Slf4j
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

        try {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("cliente", pedido.getCliente())
                    .addValue("status", pedido.getStatus().name())
                    .addValue("data_criacao", pedido.getDataCriacao());

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});

            Number key = keyHolder.getKey();
            if (key != null) pedido.setId(key.longValue());

        } catch (DataAccessException ex) {
            log.error("Erro ao salvar pedido no banco: {}", pedido, ex);
            throw new CustomException.RepositoryException(
                    ErrorCode.DB_ERROR,
                    "Falha ao salvar pedido: " + ex.getMostSpecificCause().getMessage()
            );
        }
    }

    @Override
    public List<Pedido> listar() {

        try {
            return jdbcTemplate.query("SELECT * FROM pedidos", this::map);

        } catch (DataAccessException ex) {
            log.error("Erro ao listar pedidos", ex);
            throw new CustomException.RepositoryException(
                    ErrorCode.DB_ERROR,
                    "Falha ao listar pedidos"
            );
        }
    }

    @Override
    public Optional<Pedido> buscarPorId(Long id) {
        String sql = "SELECT * FROM pedidos WHERE id = :id";
        try {
            var params = new MapSqlParameterSource("id", id);
            List<Pedido> list = jdbcTemplate.query(sql, params, this::map);
            return list.stream().findFirst();
        } catch (DataAccessException ex) {
            log.error("Erro ao buscar pedido id={}", id, ex);
            throw new CustomException.RepositoryException(
                    ErrorCode.DB_ERROR,
                    "Falha ao buscar pedido"
            );
        }
    }

    @Override
    public void atualizarStatus(Long id, StatusPedido status) {
        String sql = "UPDATE pedidos SET status = :status WHERE id = :id";
        try {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("id", id)
                    .addValue("status", status.name());
            int updated = jdbcTemplate.update(sql, params);
            if (updated == 0) {
                throw new CustomException.NotFoundException(
                        ErrorCode.ORDER_NOT_FOUND,
                        "Pedido não encontrado: " + id
                );
            }
        } catch (CustomException.NotFoundException nfe) {
            throw nfe;
        } catch (DataAccessException ex) {
            log.error("Erro ao atualizar status do pedido id={} para {}", id, status, ex);
            throw new CustomException.RepositoryException(
                    ErrorCode.DB_ERROR,
                    "Falha ao atualizar status do pedido"
            );
        }
    }
}
