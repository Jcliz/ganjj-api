jest.mock('cloudinary', () => {
  const upload_stream = jest.fn();
  return {
    v2: {
      config: jest.fn(),
      uploader: { upload_stream },
    },
  };
});

const cloudinary = require('cloudinary').v2;
const { uploadImagem } = require('../controllers/uploadController');

function mockRes() {
  const res = {};
  res.status = jest.fn().mockReturnValue(res);
  res.json = jest.fn().mockReturnValue(res);
  return res;
}

describe('uploadImagem', () => {
  test('retorna 400 se nenhum arquivo enviado', async () => {
    const req = {};
    const res = mockRes();

    await uploadImagem(req, res);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ error: 'Nenhum arquivo enviado' });
  });

  test('faz upload e retorna a URL segura', async () => {
    const req = { file: { buffer: Buffer.from('img') } };
    const res = mockRes();
    cloudinary.uploader.upload_stream.mockImplementationOnce((opts, cb) => ({
      end: () => cb(null, { secure_url: 'https://cdn.example.com/img.png' }),
    }));

    await uploadImagem(req, res);

    expect(cloudinary.uploader.upload_stream).toHaveBeenCalledWith(
      { folder: 'ganjj/produtos', resource_type: 'image' },
      expect.any(Function)
    );
    expect(res.json).toHaveBeenCalledWith({ url: 'https://cdn.example.com/img.png' });
  });

  test('retorna 500 se o upload falha', async () => {
    const req = { file: { buffer: Buffer.from('img') } };
    const res = mockRes();
    cloudinary.uploader.upload_stream.mockImplementationOnce((opts, cb) => ({
      end: () => cb(new Error('cloudinary indisponível')),
    }));

    await uploadImagem(req, res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith({ error: 'Erro ao fazer upload da imagem' });
  });
});
