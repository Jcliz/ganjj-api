const cloudinary = require('cloudinary').v2;

cloudinary.config({
    cloud_name: process.env.CLOUDINARY_CLOUD_NAME,
    api_key:    process.env.CLOUDINARY_API_KEY,
    api_secret: process.env.CLOUDINARY_API_SECRET,
});

async function uploadImagem(req, res) {
    try {
        if (!req.file) return res.status(400).json({ error: 'Nenhum arquivo enviado' });

        const result = await new Promise((resolve, reject) => {
            const stream = cloudinary.uploader.upload_stream(
                { folder: 'ganjj/produtos', resource_type: 'image' },
                (err, result) => { if (err) reject(err); else resolve(result); }
            );
            stream.end(req.file.buffer);
        });

        res.json({ url: result.secure_url });
    } catch (error) {
        console.error('[product-service] erro no upload:', error);
        res.status(500).json({ error: 'Erro ao fazer upload da imagem' });
    }
}

module.exports = { uploadImagem };
