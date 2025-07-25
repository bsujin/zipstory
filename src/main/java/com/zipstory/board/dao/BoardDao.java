package com.zipstory.board.dao;

import java.util.List;

import javax.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.zipstory.board.model.BoardVO;
import com.zipstory.board.model.CommentsVO;
import com.zipstory.board.model.FileVO;
import com.zipstory.board.model.PageVo;
import com.zipstory.board.model.PostVO;
import com.zipstory.board.model.UserVO;

@Repository
public class BoardDao implements BoardDaoI {

	
	@Resource(name = "sqlSessionTemplate")
	private SqlSessionTemplate template;
	
	//로그인
	@Override
	public int checkLogin(UserVO userVo) {
		// TODO Auto-generated method stub
		return template.selectOne("board.checkLogin", userVo);
	}

	//게시판생성
	@Override
	public int insertBoard(BoardVO boardVo) {
		// TODO Auto-generated method stub
		return template.insert("board.insertBoard", boardVo);
	}

	//게시판 전체목록조회
	@Override
	public List<BoardVO> selectBoardList() {
		// TODO Auto-generated method stub
		return template.selectList("board.selectBoardList");
	}

	//사용자 전체조회
	@Override
	public List<UserVO> selectUserList() {
		// TODO Auto-generated method stub
		return template.selectList("board.selectUserList");
	}

	//게시글 등록
	@Override
	public int insertPost(PostVO postVo) {
		// TODO Auto-generated method stub
		return template.insert("board.insertPost",postVo);
	}
	
	//사용자 정보조회
	@Override
	public UserVO selectUser(String user_id) {
		// TODO Auto-generated method stub
		return template.selectOne("board.selectUser",user_id);
	}
	
	//게시글 전체조회
	@Override
	public List<PostVO> selectPostList(PageVo pageVo) {
		// TODO Auto-generated method stub
		return template.selectList("board.selectPostList",pageVo);
	}

	//게시글 전체수
	@Override
	public int postListCnt(PageVo pageVo) {
		// TODO Auto-generated method stub
		return template.selectOne("board.postListCnt",pageVo);
	}
	
	//게시글 상세조회
	@Override
	public PostVO postView(int post_no) {
		// TODO Auto-generated method stub
		return template.selectOne("board.postView", post_no);
	}

	@Override
	public int viewsPlus(int post_no) {
		// TODO Auto-generated method stub
		return template.update("board.viewsPlus", post_no);
	}

	@Override
	public int insertReply(PostVO postVo) {
		// TODO Auto-generated method stub
		return template.insert("board.insertReply", postVo);
	}

	@Override
	public int registUser(UserVO userVo) {
		// TODO Auto-generated method stub
		return template.insert("board.registUser",userVo);
	}

	@Override
	public int checkUserId(String user_id) {
		// TODO Auto-generated method stub
		return template.selectOne("board.checkUserId",user_id);
	}

	@Override
	public int insertFile(FileVO fileVo) {
		// TODO Auto-generated method stub
		return template.insert("board.insertFile",fileVo);
	}

	@Override
	public int maxPostno() {
		// TODO Auto-generated method stub
		return template.selectOne("board.maxPostno");
	}

	@Override
	public int deletePost(int post_no) {
		// TODO Auto-generated method stub
		return template.update("board.deletePost",post_no);
	}

	@Override
	public int updatePost(PostVO postVO) {
		// TODO Auto-generated method stub
		return template.update("board.updatePost",postVO);
	}

	@Override
	public int insertComment(CommentsVO commentsVO) {
		// TODO Auto-generated method stub
		return template.insert("board.insertComment",commentsVO);
	}

	@Override
	public List<CommentsVO> selectCommentsList(int post_no) {
		// TODO Auto-generated method stub
		return template.selectList("board.selectCommentsList",post_no);
	}

	@Override
	public int deleteComments(int com_no) {
		// TODO Auto-generated method stub
		return template.update("board.deleteComments", com_no);
	}

	@Override
	public List<PostVO> searchPostList(PageVo pageVo) {
		// TODO Auto-generated method stub
		return template.selectList("board.searchPostList", pageVo);
	}

	@Override
	public List<BoardVO> boardListView() {
		// TODO Auto-generated method stub
		return template.selectList("board.boardListView");
	}

	@Override
	public int updateBoard(BoardVO boardVo) {
		// TODO Auto-generated method stub
		return template.update("board.updateBoard", boardVo);
	}

	@Override
	public List<FileVO> selectFileList(int post_no) {
		// TODO Auto-generated method stub
		return template.selectList("board.selectFileList", post_no);
	}

	@Override
	public FileVO selectFile(int file_no) {
		// TODO Auto-generated method stub
		return template.selectOne("board.selectFile",file_no);
	}

	@Override
	public int deleteFile(int file_no) {
		// TODO Auto-generated method stub
		return template.update("board.deleteFile", file_no);
	}
	
	
}
