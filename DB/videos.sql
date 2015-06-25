

--
-- Database:  videos 
--

-- --------------------------------------------------------

--
-- Table structure for table  videodetails 
--

CREATE TABLE IF NOT EXISTS  videos.videodetails  (
   videoId  varchar(250) NOT NULL,
   url  varchar(250) NOT NULL,
   thumbnail  varchar(250) NOT NULL,
   uploader  varchar(50) NOT NULL,
   tool  varchar(50) NOT NULL,
   community  varchar(50) NOT NULL,
   time  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   description  varchar(250) NOT NULL,
   language  varchar(2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table  videodetails 
--

INSERT INTO  videos.videodetails  ( videoId ,  url ,  thumbnail ,  uploader ,  tool ,  community ,  time ,  description , language ) VALUES
('1', '.localhost', 'http://i.imgur.com/HngIsi8.jpg', 'userBlaBla', 'restservice', 'acis', '2014-12-10 16:14:32', 'This is a test video', 'en'),
('2', 'url', 'http://i.imgur.com/xxAqUpz.jpg', 'sbakiu', 'restservice', 'acis', '2014-12-04 15:30:39', 'acis meeting', 'en'),
('3', 'urlFor3', 'http://i.imgur.com/UucIiQc.jpg', 'uploaderOf3', 'ahso', 'sse', '2014-12-12 10:35:43', 'This is a video of our course mates', 'en'),
('4', 'urlFromTest', 'http://i.imgur.com/ThZhD6E.jpg', 'uploader_New', 'tool', 'community', '2014-12-17 11:22:08', 'description4', 'en'),
('6', 'url4', 'http://i.imgur.com/CclmTGh.jpg', 'uploader7', 'tool', 'community', '2014-12-11 14:09:13', 'description6', 'en');

