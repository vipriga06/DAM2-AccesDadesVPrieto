const path = require('path');
const { parseXMLFile, processYoutuberData } = require('../src/load-xml-to-mongodb');

describe('parseXMLFile', () => {
  it('llegeix i converteix un XML mínim a objecte JavaScript', async () => {
    const xmlPath = path.join(__dirname, 'fixtures', 'minimal-youtubers.xml');

    const parsedData = await parseXMLFile(xmlPath);

    expect(parsedData).toMatchObject({
      youtubers: {
        youtuber: {
          id: '99',
          channel: 'Canal Minimal',
          name: 'Persona Prova'
        }
      }
    });
  });
});

describe('processYoutuberData', () => {
  it('normalitza arrays i converteix camps numèrics i dates', () => {
    const rawData = {
      youtubers: {
        youtuber: {
          id: '99',
          channel: 'Canal Minimal',
          name: 'Persona Prova',
          subscribers: '1234',
          joinDate: '2024-01-15',
          categories: {
            category: 'Education'
          },
          videos: {
            video: {
              id: 'v99',
              title: 'Vídeo de prova',
              duration: '02:30',
              views: '42',
              uploadDate: '2024-02-01',
              likes: '7',
              comments: '3'
            }
          }
        }
      }
    };

    const processedData = processYoutuberData(rawData);

    expect(processedData).toHaveLength(1);
    expect(processedData[0]).toMatchObject({
      youtuberId: '99',
      channel: 'Canal Minimal',
      name: 'Persona Prova',
      subscribers: 1234,
      categories: ['Education'],
      videos: [
        {
          videoId: 'v99',
          title: 'Vídeo de prova',
          duration: '02:30',
          views: 42,
          likes: 7,
          comments: 3
        }
      ]
    });

    expect(processedData[0].joinDate).toBeInstanceOf(Date);
    expect(processedData[0].videos[0].uploadDate).toBeInstanceOf(Date);
  });
});