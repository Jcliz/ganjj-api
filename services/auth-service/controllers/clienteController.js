const db = require('../../shared/db');

function splitName(fullName) {
    const normalized = String(fullName || '').trim().replace(/\s+/g, ' ');
    if (!normalized) return { firstName: '', lastName: '' };
    const parts = normalized.split(' ');
    const firstName = parts.shift() || '';
    const lastName = parts.join(' ');
    return { firstName, lastName };
}

function buildUserResponse(row) {
    const { firstName, lastName } = splitName(row.nome);
    return {
        id: row.id,
        firstName,
        lastName,
        email: row.email,
        role: Boolean(row.is_admin),
        status: Boolean(row.status),
        joined: row.criado_em
    };
}

function mapStatusToBoolean(status) {
    if (typeof status === 'boolean') return status;
    if (status === 'Active')   return true;
    if (status === 'Inactive') return false;
    return false;
}

function mapRoleToIsAdmin(role) {
    if (role === undefined) return false;
    return role === 'Admin';
}

async function createCliente(req, res) {
    try {
        const { firstName, lastName, email, role, status } = req.body;

        if (!firstName || !lastName || !email) {
            return res.status(400).json({ error: 'Todos os campos são obrigatórios: firstName, lastName, email' });
        }
        if (!/\S+@\S+\.\S+/.test(email)) {
            return res.status(400).json({ error: 'Email inválido' });
        }

        const statusBoolean = mapStatusToBoolean(status);
        const isAdmin = mapRoleToIsAdmin(role);

        const conn = await db.connect();
        try {
            const emailCheck = await conn.query('SELECT id FROM usuario WHERE email = $1', [email]);
            if (emailCheck.rows.length > 0) {
                return res.status(409).json({ error: 'Email já cadastrado' });
            }

            const result = await conn.query(
                `INSERT INTO usuario (nome, email, senha, is_admin, status)
                 VALUES ($1, $2, $3, $4, $5) RETURNING *`,
                [`${firstName} ${lastName}`.trim(), email, 'senha_temporaria', isAdmin ?? false, statusBoolean ?? true]
            );

            res.status(201).json(buildUserResponse(result.rows[0]));
        } finally {
            conn.release();
        }
    } catch (error) {
        console.error('Erro ao criar usuário:', error);
        res.status(500).json({ error: 'Erro ao criar usuário' });
    }
}

async function getClientes(req, res) {
    try {
        const conn = await db.connect();
        try {
            const result = await conn.query('SELECT * FROM usuario ORDER BY criado_em DESC');
            res.json(result.rows.map(buildUserResponse));
        } finally {
            conn.release();
        }
    } catch (error) {
        console.error('Erro ao buscar usuários:', error);
        res.status(500).json({ error: 'Erro ao buscar usuários' });
    }
}

async function getClienteById(req, res) {
    try {
        const { id } = req.params;
        const conn = await db.connect();
        try {
            const result = await conn.query('SELECT * FROM usuario WHERE id = $1', [id]);
            if (result.rows.length === 0) return res.status(404).json({ error: 'Usuário não encontrado' });
            res.json(buildUserResponse(result.rows[0]));
        } finally {
            conn.release();
        }
    } catch (error) {
        console.error('Erro ao buscar usuário:', error);
        res.status(500).json({ error: 'Erro ao buscar usuário' });
    }
}

async function updateCliente(req, res) {
    try {
        const { id } = req.params;
        const { firstName, lastName, email, role, status } = req.body;

        const updates = [];
        const values = [];
        let index = 1;

        const statusBoolean = mapStatusToBoolean(status);
        const isAdmin = mapRoleToIsAdmin(role);

        if (email !== undefined && !/\S+@\S+\.\S+/.test(email)) {
            return res.status(400).json({ error: 'Email inválido' });
        }

        let nomeCompleto;
        if (firstName !== undefined || lastName !== undefined) {
            const conn = await db.connect();
            try {
                const currentUser = await conn.query('SELECT nome FROM usuario WHERE id = $1', [id]);
                if (currentUser.rows.length === 0) return res.status(404).json({ error: 'Usuário não encontrado' });
                const split = splitName(currentUser.rows[0].nome);
                const finalFirstName = firstName !== undefined ? firstName : split.firstName;
                const finalLastName  = lastName  !== undefined ? lastName  : split.lastName;
                nomeCompleto = `${finalFirstName} ${finalLastName}`.trim();
            } finally {
                conn.release();
            }
        }

        if (nomeCompleto !== undefined) { updates.push(`nome = $${index++}`);     values.push(nomeCompleto); }
        if (email        !== undefined) { updates.push(`email = $${index++}`);    values.push(email); }
        if (role         !== undefined) { updates.push(`is_admin = $${index++}`); values.push(isAdmin); }
        if (status       !== undefined) { updates.push(`status = $${index++}`);   values.push(statusBoolean); }

        if (updates.length === 0) return res.status(400).json({ error: 'Nenhum campo para atualizar' });

        values.push(id);

        const conn = await db.connect();
        try {
            if (email !== undefined) {
                const emailInUse = await conn.query('SELECT id FROM usuario WHERE email = $1 AND id != $2', [email, id]);
                if (emailInUse.rows.length > 0) return res.status(409).json({ error: 'Email já cadastrado' });
            }

            const result = await conn.query(
                `UPDATE usuario SET ${updates.join(', ')} WHERE id = $${index} RETURNING *`,
                values
            );
            if (result.rows.length === 0) return res.status(404).json({ error: 'Usuário não encontrado' });
            res.json(buildUserResponse(result.rows[0]));
        } finally {
            conn.release();
        }
    } catch (error) {
        console.error('Erro ao atualizar usuário:', error);
        res.status(500).json({ error: 'Erro ao atualizar usuário' });
    }
}

async function deleteCliente(req, res) {
    try {
        const { id } = req.params;
        const conn = await db.connect();
        try {
            const result = await conn.query('DELETE FROM usuario WHERE id = $1 RETURNING id', [id]);
            if (result.rows.length === 0) return res.status(404).json({ error: 'Usuário não encontrado' });
            res.json({ message: 'Usuário deletado com sucesso' });
        } finally {
            conn.release();
        }
    } catch (error) {
        console.error('Erro ao deletar usuário:', error);
        res.status(500).json({ error: 'Erro ao deletar usuário' });
    }
}

module.exports = { createCliente, getClientes, getClienteById, updateCliente, deleteCliente };
